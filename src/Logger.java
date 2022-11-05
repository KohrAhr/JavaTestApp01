import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/*
  Logger functionality
  Write text to file
    X:\FOLDER\SUB-FOLDER\TIMESTAMP\TIMSTAMP PROJECTNAME.log
*/
public class Logger
{
  // Yes, could be situtation that session was started before midnight, but finished after midnight. As you see, in such cases everything will be stored in "previous day".
  private String outputFile = "";
  private LogWay logWay;
  private String splitter;

  // Constuctor
  Logger(LoggerParams loggerInParams) throws IOException
  {
    if (loggerInParams.outputFolder.isEmpty() || loggerInParams.fileNameFormat.isEmpty() || loggerInParams.objectName.isEmpty() || loggerInParams.fileNameExtension.isEmpty())
    {
      throw new RuntimeException("Parameters cannot be empty");
    }

    outputFile = ResolveFullFileName(loggerInParams);
    Helper.CreateFolder(ResolveFullPath(loggerInParams));
    logWay = loggerInParams.logWay;
    splitter = loggerInParams.splitter;
  }

  /*
    Get previosly resolved file name
  */
  public String GetFileName()
  {
    return outputFile;
  }

  /*
    Resolve full path and log file name for today  
  */
  public String ResolveFullFileName(LoggerParams loggerInParams)
  {
    String timeStamp = Helper.GetFormattedDate(loggerInParams.fileNameFormat);
    String folder = ResolveFullPath(loggerInParams);

    String result = loggerInParams.template;
    result = result.replace(LoggerConsts.CONST_PATTERN_FOLDER, folder).
      replace(LoggerConsts.CONST_PATTERN_TIMESTAMP, timeStamp).
      replace(LoggerConsts.CONST_PATTERN_FILENAME, loggerInParams.objectName).
      replace(LoggerConsts.CONST_PATTERN_FILEEXTENSION, loggerInParams.fileNameExtension);

    return result;
  }

  /*
    Resolve full path for today  
    Single responsibility
  */
  public String ResolveFullPath(LoggerParams loggerInParams)
  {
    String timeStamp = Helper.GetFormattedDate(loggerInParams.fileNameFormat);
    String folder = Helper.CheckAndFixEndingFolderSeparator(loggerInParams.outputFolder);

    folder = folder.replace(LoggerConsts.CONST_PATTERN_TIMESTAMP, timeStamp);

    return folder;
  }

  /*
    Write to file, low level code
  */
  private void LogByUsingFileWriter(String value)
  {
    try
    {
      FileWriter fileWriter = new FileWriter(outputFile, true);
      fileWriter.write(LogValueFormatted(value));
      fileWriter.flush();
      fileWriter.close();
    }
    catch (IOException e) 
    {
      // System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  /*
    Write to file, low level code
  */
  private void LogByUsingStreamWriter(String value)
  {
    try
    {
      File file = new File(outputFile);
      FileOutputStream fos = new FileOutputStream(file, true);
      OutputStreamWriter osw = new OutputStreamWriter(fos);
      osw.write(LogValueFormatted(value));
      osw.close();
    }
    catch (IOException e) 
    {
      // System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  private String LogValueFormatted(String value)
  {
    return LoggerConsts.CONST_OUTPUT_PATTERN.replace(LoggerConsts.CONST_PATTERN_TIMESTAMP, Helper.GetFormattedDateForLog()).
      replace(LoggerConsts.CONST_PATTERN_SPLITTER, splitter).
      replace(LoggerConsts.CONST_PATTERN_VALUE, value);

//    return String.format("%s%s%s\n", new Date().toString(), splitter, value);
  }

  /*
    Write to file, top level code
  */
  public void Log(final String value)
  {
    switch (logWay) 
    {
      case FILE_WRITTER:
      {
        /*
          Way 1. FileWriter & PrintWriter
        */
        LogByUsingFileWriter(value);

        break;
      }
      case STREAM_WRITTER:
      {
        /*
          Way 2.
        */
        LogByUsingStreamWriter(value);

        break;
      }
    }
  }
}