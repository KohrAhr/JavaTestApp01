/*
  Compatible with 1.8.0_351
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import com.microsoft.sqlserver.jdbc.SQLServerException;

public class CrazyDb 
{
  // 1st attempt we always make. 
  // So, 2 mean that we handle error only once. This is not 2 additional attempt to first one, it's two attempt at all. 
  // So, 6 mean that we handle error five times. First obligatory run plus 5 extra attempts.
  // IMHO we should have not less than 3 attempts and not more than 6 for sure
  public static int CONST_MAX_ATTEMPTS = 5;

  /*
    Low level handler for errors
  */
  private static int errorHandler(int aErrorCounter, SQLException aE) throws InterruptedException, SQLException
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

  /*
    Top level handler for errors
  */
  public static int handleSqlException(int aErrorCounter, SQLException aE) throws InterruptedException, SQLException
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
      // Incorrect syntax
      102,
      // Other unknown?
      0
    };

    int errorCode = aE.getErrorCode();
    // Maybe in future... when ErrorCode=0 this info will help us
    // String sqlError = aE.getSQLState();

    if (Arrays.stream(codes).anyMatch(x -> x == errorCode))
    {
      throw aE;
    }
    else
    {
      aErrorCounter = errorHandler(aErrorCounter, aE);
    }

    return aErrorCounter;
  }

  /*
    Run SQL-Query and return result (DataSet)
  */
  public static ResultSet getResultSet(Connection aConnection, String aSqlQuery) throws InterruptedException, SQLException
  {
    // reset error counter and Completed status
    int errorCounter = 0;
    boolean completed = false;
    ResultSet resultSet = null;

    // WHo cares? Really?
    // Yes, outside try (...) and without (are you sure) GC?
    // +1h
    Statement statement = aConnection.createStatement();
    do
    {
      try
      {
        resultSet = statement.executeQuery(aSqlQuery);

        // Permission to leave from Do-While block
        completed = true;
      }
      // Swallow SQL server connection error. Handle it on special way.
      catch (SQLServerException e) 
      {
        errorCounter = handleSqlException(errorCounter, e);
      }    
    } 
    /*
      Continue until:
      1) we didn't hit maximum attempts
      2) connection cannot be established because of the exception
    */
    while (errorCounter < CONST_MAX_ATTEMPTS && !completed);
    // #endregion

    return resultSet;
  }
  
  /*
    Establish connection
  */
  public static Connection doConnect(String aConnection) throws InterruptedException, SQLException
  {
    // reset error counter and Completed status
    int errorCounter = 0;
    boolean completed = false;
    Connection connection = null;
    
    do
    {
      try
      {
        connection = DriverManager.getConnection(aConnection);
        
        // Permission to leave from Do-While block
        completed = true;
      }
      // Swallow SQL server connection error. Handle it on special way.
      catch (SQLServerException e) 
      {
        errorCounter = handleSqlException(errorCounter, e);
      }    
    } 
    /*
      Continue until:
      1) we didn't hit maximum attempts
      2) connection cannot be established because of the exception
    */
    while (errorCounter < CONST_MAX_ATTEMPTS && !completed);

    return connection;
  }
}