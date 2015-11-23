import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Connie Shi (cs3313@nyu.edu)
 * Module is a module represented in the input as the definition, use list, and program text.
 */
public class Module {
  private int moduleNumber;
  private ProgramText[] programText;
  private int offset;
  private int size;
  private HashMap<Integer, ArrayList<String>> mapUseToSymbol;
  private String errorMessage = "";

  public Module(int moduleNumber, String[] programText, int offset, ArrayList<String> useList) {
    this.moduleNumber = moduleNumber;
    this.offset = offset;
    this.size = programText.length;
    mapUseToSymbol = new HashMap<Integer, ArrayList<String>>();
    createProgramText(programText);
    createNameMapping(useList);
  }

  /**
   * @param programTextString
   * Create ProgramText from program text string
   */
  private void createProgramText(String[] programTextString) {
    programText = new ProgramText[programTextString.length];
    for (int i = 0; i < programTextString.length; i++) {
      ProgramText text = new ProgramText(programTextString[i], i + offset);
      programText[i] = text;
    }
  }

  /**
   * @param useList
   * Map every use index to a list of variables that have the index within its use list
   * Example: use list -> "2 xy 1 2 -1 z 1 -1"
   * Map: { 1:[xy, z], 2:[xy] }
   */
  private void createNameMapping(ArrayList<String> useList) {
    if (useList.size() > 0) {
      int numUses = Integer.parseInt(useList.get(0));
      int index = 1;
      while (numUses-- > 0) {
        String var = useList.get(index++);
        while (true) {
          int currentNum = Integer.parseInt(useList.get(index++));
          if (currentNum == -1) {
            break;
          }
          if (!mapUseToSymbol.containsKey(currentNum + offset)) {
            mapUseToSymbol.put(currentNum + offset, new ArrayList<String>());
          }
          mapUseToSymbol.get(currentNum + offset).add(var);
        }
      }
    }
    checkUseList();
  }
  
  /**
   * Checks the symbols in use list does not exceed module size.
   */
  private void checkUseList() {
    for (Entry<Integer, ArrayList<String>> entry : mapUseToSymbol.entrySet()) {
      if (entry.getKey() >= offset + size) {
        errorMessage += "Error: Use of " + entry.getValue() + " in module " + moduleNumber 
            + " exceeds module size; use ignored. ";
      }
    }
  }

  /**
   * @return module number
   */
  public int getModuleNumber() {
    return moduleNumber;
  }

  /**
   * @return ProgramText list
   */
  public ProgramText[] getProgramText() {
    return programText;
  }

  /**
   * @return offset of all previous modules
   */
  public int getOffset() {
    return offset;
  }
  
  /**
   * @return size of current module
   */
  public int getSize() {
    return size;
  }

  /**
   * @return mapping of indices to symbol(s) used
   */
  public HashMap<Integer, ArrayList<String>> getUseToSymbol() {
    return mapUseToSymbol;
  }

  /**
   * @return error message
   */
  public String getErrorMessage() {
    return errorMessage;
  }
}
