class LoggerConsts 
{
  public static final String CONST_PATTERN_FOLDER = "{%Folder%}";
  public static final String CONST_PATTERN_TIMESTAMP = "{%TimeStamp%}";
  public static final String CONST_PATTERN_FILENAME = "{%FileName%}";
  public static final String CONST_PATTERN_FILEEXTENSION = "{%FileExtension%}";

  public static final String CONST_PATTERN_SPLITTER = "{%Splitter%}";
  public static final String CONST_PATTERN_VALUE = "{%Value%}";

  // 4 digit Year, 2 digit Month, 2 digit Day
  public static final String CONST_FILE_NAME_FORMAT = "YYYYMMdd";
  // File name extension without leading dot
  public static final String CONST_FILE_NAME_EXTENSION = "log";

  public static final String CONST_SPLITTER_DEFAULT_VALUE = "\n";

  // String CONST_OUTPUTFILE_DEFAULT_VALUE = "{%Folder%}{%TimeStamp%} {%FileName%}.{%FileExtension%}" 
  //  --> 
  //    C:\Temp\DD\20220722\ 
  //    20220722
  //    FILENAME
  //    TXT
  public static final String CONST_OUTPUTFILE_DEFAULT_VALUE = "{%Folder%}{%TimeStamp%} {%FileName%}.{%FileExtension%}";

  public static final String CONST_OUTPUT_PATTERN = "{%TimeStamp%}{%Splitter%}{%Value%}\n";

  public static final String CONST_DATEFORMAT_IN_LOG = "yyyy-MM-dd HH:mm:ss.SSS";
}
