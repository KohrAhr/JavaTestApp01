import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;


public class Helper 
{
  private static final int CONST_MIN_LENGTH = 42;

  public static String ResultFormat(String AValue)
  {
    /* 
      For Version 8 
    */
    while (AValue.length() < CONST_MIN_LENGTH)
    {
      AValue += " ";
    }
    /* 
      For Version 17 
    */
    // int x = CONST_MIN_LENGTH - AValue.length();
    // if (AValue.length() < CONST_MIN_LENGTH)
    // {
    //   AValue += " ".repeat(x);
    // }
    
     return AValue;
  }

  public static void CreateFolder(String folder) throws IOException
  {
    File theDir = new File(folder);
    try
    {
      if (!theDir.exists())
      {
          theDir.mkdirs();
      }

      // 2nd check that output folder exist
      if (!theDir.exists())
      {
  //        System.out.println("An error occurred.");
        throw new IOException("Cannot create output folder: " + folder);
      }
    }
    catch(IOException e)
    {
      throw new IOException("Cannot create output folder: " + folder);
    }
 }

  /*
    Current date to YYYYMMdd format
  */
  public static String GetFormattedDate(String aDateFormat)
  {
    return new SimpleDateFormat(aDateFormat).format(new Date());
  }

  /*
    Current date in detailed format
  */
  public static String GetFormattedDateForLog()
  {
    return GetFormattedDate(LoggerConsts.CONST_DATEFORMAT_IN_LOG);
  }

  /*
    Just to be sure that filder defined in right format
    Folder delimeter "/" or "\" at the end
  */
  public static String CheckAndFixEndingFolderSeparator(String outputFolder)
  {
    String fileSeparator = System.getProperty("file.separator");
    if (!outputFolder.endsWith(fileSeparator))
    {
      outputFolder += fileSeparator;
    }
    return outputFolder;
  }
  
}
