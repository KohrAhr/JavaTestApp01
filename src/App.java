import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
//import java.util.concurrent.Callable;
//import java.util.function.Function;
import java.util.Properties;

import com.microsoft.sqlserver.jdbc.SQLServerException;

//import org.xml.sax.ErrorHandler;

public class App 
{
  // Output folder should end with separator
  private static final String CONST_OUTPUT_FOLDER = "C:\\Temp\\DD\\{%TimeStamp%}\\";

  // 1st attempt we always make. 
  // So, 2 mean that we handle error only once. This is not 2 additional attempt to first one, it's two attempt at all. 
  // So, 6 mean that we handle error five times. First obligatory run plus 5 extra attempts.
  // IMHP we should have not less than 3 attempts and not more than 6 for sure
  private static final int CONST_MAX_ATTEMPTS = 5;

  public static int errorHandler(int aErrorCounter, SQLException aE) throws InterruptedException, SQLException
  {
    // Counter of errors.
    aErrorCounter++;

    System.out.println(String.format("Cannot connect to database. Attempt No %s failed", aErrorCounter));
    System.out.println(String.format("Error message is: %s", aE.getMessage()));

    // Did we make already enought attempts to re-run?
    if (aErrorCounter >= CONST_MAX_ATTEMPTS)
    {
      /*
        Show detailed error
        No needs to show detailed error messsage here anymore, because in next list we call exception and error message will appear in main thread
      */
      // aE.printStackTrace();

      /* Bye bye */
      throw new SQLException(aE);
    }

    // Delays in seconds on attempt; Formula;
    // 1  4   9   16  25  = 55 sec total all attempts.  x * x
    // 4  9   16  25  36  = 90 sec total all attempts.  (x + 1) * (x + 1)
    // 2  6   12  20  30  = 70 sec total all attempts.  (x + 1) * x
    // 3  8   15  24  35  = 85 sec total all attempts.  (x + 2) * x

    // Delay Formula: (x + 1) * (x + 1)
    // 4 sec after 1st failure
    // 9 sec after 2nd failure
    // 16 sec after 3rd failure

    // Use Formula you like to calculate delay interval on this iteration step
    int delay = (aErrorCounter + 1) * aErrorCounter;

    // Funny sleep timer
    System.out.println(String.format("Delay in seconds before another attempt is %s", delay));
    System.out.print("Sleep");
    for (int i = 0;i< delay;i++)
    {
      // InterruptedException
      Thread.sleep(1000);
      
      System.out.print(".");
    }
    System.out.println();

    return aErrorCounter;
  }

//   public static void safeRun(Object f) throws InterruptedException, SQLException
//   {
//     int errorCounter = 0;
//     boolean completed = false;

//     completed = false;
//     do
//     {
//       try
//       {
// //        f.invoke();

//         completed = true;
//       }
//       // Swallow SQL server connection error. Handle it on special way.
//       catch (SQLException e) 
//       {
// //        errorCounter = errorHandler(errorCounter, e);
//       }    
//     } while (errorCounter < CONST_MAX_ATTEMPTS || completed);
//   }

  // public interface MyInterface {
  //   void doSomething();
  // }  


  public static int HandleSqlException(int aErrorCounter, SQLException aE) throws InterruptedException, SQLException
  {
    // Ok, at this moment we are retrying all errors, but if it's Uid/Pwd incorrect, than we don't have to retry and we should fail immidiately
    // 18456 -- Login failed
    // 4060 -- Wrong Default Db
    // https://learn.microsoft.com/en-us/azure/azure-sql/database/troubleshoot-common-errors-issues?view=azuresql

    int[] codes = 
    {
      // Login failed
      18456,
      // Wrong default db
      4060,
      // Other unknown?
      0
    };

    int errorCode = aE.getErrorCode();
    // Maybe in future... when ErrorCode=0 this info will help us
    // String sqlError = aE.getSQLState();

    if (Arrays.stream(codes).anyMatch(x -> x == errorCode))
    {
      aErrorCounter = errorHandler(aErrorCounter, aE);
    }
    else
    {
      throw aE;
    }

    return aErrorCounter;
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
        String.format("%s: %s", Helper.ResultFormat(item), System.getProperty(item))
      );
    }

    // Read config file
    Properties properties = new Properties();
    String fileName = "App.config";
    try (FileInputStream configFile = new FileInputStream(fileName)) 
    {
      properties.load(configFile);
    } 
    catch (IOException e) 
    {
      throw e;  
    }

    final String connectionUrl = properties.getProperty("ConnString");

    ResultSet resultSet = null;
    Connection connection = null;
    int errorCounter = 0;
    boolean completed = false;

    /*
      Establish connection
      Please do not optimize it and do not merge it with connection.createStatement() function. 
      Reason: we really want to catch and have possibility to distinguish problem with initial connection and future executions
    */
    do
    {
      System.out.println("Trying to connect to SQL server..");
      try
      {
        connection = DriverManager.getConnection(connectionUrl);
        
        // Leave from Do-While block
        completed = true;
      }
      // Swallow SQL server connection error. Handle it on special way.
      catch (SQLServerException e) 
      {
        errorCounter = HandleSqlException(errorCounter, e);
      }    
    } 
    /*
      Or try: (errorCounter < CONST_MAX_ATTEMPTS && connection == null)
      
      Continue until:
      1) we didn't hit maximum attempts
      2) connection cannot be established because of the exception
    */
    while (errorCounter < CONST_MAX_ATTEMPTS && !completed);

    // reset error counter and Completed status
    errorCounter = 0;
    completed = false;

    // 1st SQL command
    final String selectSql = "select @@VERSION as E1 union all SELECT CAST(@@CONNECTIONS as VARCHAR);";
    completed = false;
    do
    {
      try (Statement statement = connection.createStatement())
      {
        resultSet = statement.executeQuery(selectSql);

        while (resultSet.next()) 
        {
          System.out.println(resultSet.getString(1));
        }

        // Permission to leave from Do-While block
        completed = true;
      }
      // Swallow SQL server connection error. Handle it on special way.
      catch (SQLServerException e) 
      {
        errorCounter = HandleSqlException(errorCounter, e);
      }    
    } 
    /*
      Continue until:
      1) we didn't hit maximum attempts
      2) connection cannot be established because of the exception
    */
    while (errorCounter < CONST_MAX_ATTEMPTS && !completed);

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
