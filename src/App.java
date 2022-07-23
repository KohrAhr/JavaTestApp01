public class App 
{
  // Output folder should end with separator
  private static final String CONST_OUTPUT_FOLDER = "C:\\Temp\\DD\\{%TimeStamp%}\\";

  public static void main(String[] args) throws Exception 
  {
    System.out.println(Helper.GetFormattedDateForLog());

    LoggerParams loggerParams = new LoggerParams(
      CONST_OUTPUT_FOLDER, 
      LoggerConsts.CONST_FILE_NAME_FORMAT, 
      "ABRA_CAD_ABRA", 
      LoggerConsts.CONST_FILE_NAME_EXTENSION, 
      LogWay.STREAM_WRITTER
    );
    
    Logger universe = new Logger(loggerParams);
    System.out.println(universe.GetFileName());

    // Dummy logic
    int CONST_REPEAT_COUNT = 1000;
    for (int x = 0;x < 100;x ++)
    {
      universe.Log("Test 1".repeat(CONST_REPEAT_COUNT));
      universe.Log("Test 2".repeat(CONST_REPEAT_COUNT));
      universe.Log("Test 3".repeat(CONST_REPEAT_COUNT));
    }

    // Equal commands
    //System.out.println(universe.ResolveFullFileName(loggerParams));
    // Equal commands
    System.out.println(Helper.GetFormattedDateForLog());
  }
}
