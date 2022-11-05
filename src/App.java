// CLASSPATH=.;C:\Program Files\Microsoft JDBC Driver 8.4 for SQL Server\sqljdbc_8.4\enu\mssql-jdbc-8.4.1.jre11.jar

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App 
{
  // Output folder should end with separator
  private static final String CONST_OUTPUT_FOLDER = "C:\\Temp\\DD\\{%TimeStamp%}\\";

  private static final int CONST_MIN_LENGTH = 42;
  // 1st attempt we always make. 
  // So, 2 mean that we handle error only once. This is not 2 additional attempt to first one, it's two attempt at all. 
  private static final int CONST_MAX_ATTEMPTS = 3;

  public static String ResultFormat(String AValue)
  {
    int x = CONST_MIN_LENGTH - AValue.length();
    if (AValue.length() < CONST_MIN_LENGTH)
    {
      AValue += " ".repeat(x);
    }
    return AValue;
  }

  public static void main(String[] args) throws Exception 
  {
    String[] variables = 
    {
      "java.version", 
      "java.version.date", 
      "java.vendor",
      "java.specification.name",
      "java.specification.vendor",
      "java.specification.version",
      "java.vm.name",
      "java.vm.vendor",
      "java.vm.version",
      "java.vm.info",
      "java.vm.specification.name",
      "java.vm.specification.vendor",
      "java.vm.specification.version",
      "java.runtime.name",
      "java.runtime.version",
      "java.class.version",
      "jdk.debug",
      "sun.java.launcher",
      "java.class.version",
      "sun.management.compiler"
    };

    System.out.println(Helper.GetFormattedDateForLog());
    for (String item : variables) 
    {
      System.out.println
      (
        String.format("%s: %s", ResultFormat(item), System.getProperty(item))
      );
    }

    final String connectionUrl = "jdbc:sqlserver://localhost:1433;database=;user=;password=;encrypt=false;trustServerCertificate=true;loginTimeout=30;";

    ResultSet resultSet = null;
    Connection connection = null;
    int delay = 0;

    int errorCounter = 0;
    do
    {
      try
      {
        connection = DriverManager.getConnection(connectionUrl);
        
        // Leave from Do-While block
        break;
      }
      // Shallow SQL server connection error. Handle it on special way.
      catch (SQLException e) 
      {
        // Counter of errors.
        errorCounter++;

        System.out.println(String.format("Cannot connect to database. Attempt No %s failed", errorCounter));
        System.out.println(String.format("Error message is: %s", e.getMessage()));

        // Did we make already enought attempts to re-run?
        if (errorCounter >= CONST_MAX_ATTEMPTS)
        {
          // Show detailed error
          e.printStackTrace();

          // Bye bye
          throw new Exception();
        }

        // Delays in seconds on attempt; Formula;
        // 1  4   9   16  25  = 55 sec total all attempts.  x * x
        // 4  9   16  25  36  = 90 sec total all attempts.  (x + 1) * (x + 1)
        // 2  6   12  20  30  = 70 sec total all attempts.  (x + 1) * x

        // Delay Formula: (x + 1) * (x + 1)
        // 4 sec after 1st failure
        // 9 sec after 2nd failure
        // 16 sec after 3rd failule

        // Use Formula you like to calculate delay interval on this iteration step
        delay = (errorCounter + 1) * errorCounter;

        // Funny sleep timer
        System.out.print("Sleep");
        System.out.println(String.format("Delay in seconds before another attempt is %s", delay));
        for (int i = 0;i< delay;i++)
        {
          Thread.sleep(1000);
          System.out.print(".");
        }
        System.out.println();
      }    
    } while (!(errorCounter >= CONST_MAX_ATTEMPTS));

    // 1st SQL command
    try (Statement statement = connection.createStatement())
    {
      final String selectSql = "select @@VERSION as E1 union all SELECT CAST(@@CONNECTIONS as VARCHAR);";
      resultSet = statement.executeQuery(selectSql);

      while (resultSet.next()) 
      {
        System.out.println(resultSet.getString(0) + " " + resultSet.getString(1));
      }
    }
    catch (SQLException e) 
    {
      e.printStackTrace();
      throw new Exception();
    }    

//    System.out.println(Runtime.version());

    LoggerParams loggerParams = new LoggerParams
    (
      CONST_OUTPUT_FOLDER,
      LoggerConsts.CONST_FILE_NAME_FORMAT,
      "ABRA_CAD_ABRA",
      LoggerConsts.CONST_FILE_NAME_EXTENSION,
      LogWay.STREAM_WRITTER
    );

    Logger universe = new Logger(loggerParams);
    System.out.println(universe.GetFileName());

    // #region Dummy logic
    int CONST_REPEAT_COUNT = 1000;
    for (int x = 0; x < 100; x++) 
    {
      universe.Log("Test 1".repeat(CONST_REPEAT_COUNT));
      universe.Log("Test 2".repeat(CONST_REPEAT_COUNT));
      universe.Log("Test 3".repeat(CONST_REPEAT_COUNT));
    }
    // #endregion

    System.out.println(Helper.GetFormattedDateForLog());
  }
}
