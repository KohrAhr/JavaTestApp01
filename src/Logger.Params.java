/*
  Data class (IN parameters) for Logger class
*/
class LoggerParams
{
  public String outputFolder = "";
  public String fileNameFormat = "";
  public String objectName = "";
  public String fileNameExtension = "";
  public LogWay logWay = LogWay.FILE_WRITTER;
  public String splitter = LoggerConsts.CONST_SPLITTER_DEFAULT_VALUE;
  public String template = LoggerConsts.CONST_OUTPUTFILE_DEFAULT_VALUE;

  LoggerParams(String aOutputFolder, String aFileNameFormat, String aObjectName, String aFileNameExtension, LogWay aLogWay, String aSplitter, String aTemplate)
  {
    // Initial set as it as
    template = aTemplate;
    splitter = aSplitter;
    _LoggerParams(aOutputFolder, aFileNameFormat, aObjectName, aFileNameExtension, aLogWay);
  }

  LoggerParams(String aOutputFolder, String aFileNameFormat, String aObjectName, String aFileNameExtension, LogWay aLogWay, String aSplitter)
  {
    // Initial set as it as
    splitter = aSplitter;
    _LoggerParams(aOutputFolder, aFileNameFormat, aObjectName, aFileNameExtension, aLogWay);
  }

  LoggerParams(String aOutputFolder, String aFileNameFormat, String aObjectName, String aFileNameExtension, LogWay aLogWay)
  {
    _LoggerParams(aOutputFolder, aFileNameFormat, aObjectName, aFileNameExtension, aLogWay);
  }

  private void _LoggerParams(String aOutputFolder, String aFileNameFormat, String aObjectName, String aFileNameExtension, LogWay aLogWay)
  {
    // Initial set as it as
    outputFolder = aOutputFolder;
    fileNameFormat = aFileNameFormat;
    objectName = aObjectName;
    fileNameExtension = aFileNameExtension;
    logWay = aLogWay;
  }
}
