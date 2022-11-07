/*
  Compatible with 1.8.0_351
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
//import java.util.concurrent.Callable;
//import java.util.function.Function;
import java.util.Hashtable;

public class App 
{
  // Output folder should end with separator
  //private static final String CONST_OUTPUT_FOLDER = "C:\\Temp\\DD\\{%TimeStamp%}\\";

  // 1st attempt we always make. 
  // So, 2 mean that we handle error only once. This is not 2 additional attempt to first one, it's two attempt at all. 
  // So, 6 mean that we handle error five times. First obligatory run plus 5 extra attempts.
  // IMHO we should have not less than 3 attempts and not more than 6 for sure
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

    // #region Read and proceed Config file
    String fileName = "App.config";
    try (FileInputStream configFile = new FileInputStream(fileName)) 
    {
      AppData.appProperties.load(configFile);
    } 
    catch (IOException e) 
    {
      throw e;   
    }

    AppData.dbConnectionString = AppData.appProperties.getProperty("ConnStringMSSQL");
//    String ConnStringMSSQL2 = AppData.appProperties.getProperty("ConnStringMSSQL2");
    // #endregion

    Hashtable<String, Connection> connections = new Hashtable<String, Connection>();
    Connection connection1, connection2 = null;
    
    // :)
    CrazyDb.CONST_MAX_ATTEMPTS = 3;

    /*
      Establish connection #1
    */
    System.out.println("Trying to connect to MS SQL Server 2019. Session 1...");
    connections.put(
      "MSSQLSession1", 
      CrazyDb.doConnect(AppData.dbConnectionString)
    );

    /*
      Establish connection #2
    */
    System.out.println("Trying to connect to MS SQL Server 2019. Session 2...");
    connections.put(
      "MSSQLSession2", 
      CrazyDb.doConnect(AppData.dbConnectionString)
    );

    // #region 1st SQL command via 1st Session
    final String selectSql = "select @@SPID;";

    connection1 = connections.get("MSSQLSession1");
    try (ResultSet resultSet = CrazyDb.getResultSet(connection1, selectSql))
    {
      while (resultSet.next()) 
      {
        System.out.println(resultSet.getString(1));
      }
      resultSet.close();
    }
    // #endregion

    // #region 1st SQL command via 2nd Session
    connection2 = connections.get("MSSQLSession2");
    // Really, who cares about Statement ?
    try (ResultSet resultSet = CrazyDb.getResultSet(connection2, selectSql))
    {
      while (resultSet.next()) 
      {
        System.out.println(resultSet.getString(1));
      }
      resultSet.close();
    }
    // #endregion

//    System.out.println(Runtime.version());

    // LoggerParams loggerParams = new LoggerParams
    // (
    //   CONST_OUTPUT_FOLDER,
    //   LoggerConsts.CONST_FILE_NAME_FORMAT,
    //   "ABRA_CAD_ABRA",
    //   LoggerConsts.CONST_FILE_NAME_EXTENSION,
    //   LogWay.STREAM_WRITTER
    // );

    // Logger universe = new Logger(loggerParams);
    // System.out.println(universe.GetFileName());

    // // #region Dummy logic
    // // int CONST_REPEAT_COUNT = 1000;
    // // for (int x = 0; x < 100; x++) 
    // // {
    // //   universe.Log("Test 1".repeat(CONST_REPEAT_COUNT));
    // //   universe.Log("Test 2".repeat(CONST_REPEAT_COUNT));
    // //   universe.Log("Test 3".repeat(CONST_REPEAT_COUNT));
    // // }
    // // #endregion

    /*
      Generate DEADLOCK situation
    */

    /*
      begin transaction
	      INSERT INTO [dbo].[Test1] ([PName] ,[PValue])
        VALUES ('Name100', 'Value100')
    */

    System.out.println(Helper.GetFormattedDateForLog());
  }

}
