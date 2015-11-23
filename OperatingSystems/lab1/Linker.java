import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * @author Connie Shi (cs3313@nyu.edu)
 * Linker object contains a list of module, a Symbol Table, and error messages.
 */
public class Linker {
  private static final int MAX_SIZE = 300;
  private static final int LINKED_TEXT_SIZE = 4;
  private static final int DEFAULT_SYMBOL_LOCATION = 111;
  private static final int FIRST_DIGIT_MULTIPLIER = 1000;
  
  private LinkedHashMap<String, Symbol> symbolTable;
  private String errorMessage = "";
  private ArrayList<Module> moduleList;

  public Linker() {
    moduleList = new ArrayList<Module>();
    symbolTable = new LinkedHashMap<String, Symbol>();
  }

  /**
   * @param symbol
   * @param location
   * @param module
   * @return Symbol inserted into the Symbol Table
   * Adds a symbol with it's name, addressField, and module number.
   */
  public Symbol addSymbol(String symbol, int location, int module) {
    if (symbolTable.containsKey(symbol)) {
      String message = "Error: This variable is multiply defined; last value used. ";
      Symbol symbolFound = symbolTable.get(symbol);
      symbolFound.addErrorMessage(message);
      symbolFound.setLocation(location);
    } else {
      symbolTable.put(symbol, new Symbol(symbol, location, module));
    }
    return symbolTable.get(symbol);
  }

  /**
   * During the 2nd pass, generate the resulting memory mappings for all modules and all unresolved
   * unlinked program text.
   */
  public void getResultAddresses() {
    
    for (Module module : moduleList) {
      for (ProgramText programText : module.getProgramText()) {
        switch (programText.getType()) {
          case IMMEDIATE:
            programText.setLinkedText(programText.getUnlinkedText().substring(0, LINKED_TEXT_SIZE));
            break;
          case ABSOLUTE: 
            checkAbsolute(module, programText); 
            break;
          case RELATIVE: 
            relocateRelative(module, programText); 
            break;
          case EXTERNAL: 
            resolveExternal(module, programText); 
            break;
          case ERROR:
            System.out.println("FATAL ERROR: The last digit of the address is not 1, 2, 3, or 4. "
                + "EXITING...");
            System.exit(1);
            break;
        }
      }
    }
  }

  /**
   * @param module
   * @param programText
   * Check that absolute address is still within memory limit, then return the first 4 digits.
   */
  private void checkAbsolute(Module module, ProgramText programText) {
    String address = programText.getUnlinkedText();
    int firstNumber = Character.getNumericValue(address.charAt(0));
    int addressField = Integer.parseInt(address.substring(1, LINKED_TEXT_SIZE));
    
    // Check that memory limit is not exceeded
    if (addressField >= MAX_SIZE) {
      programText.addErrorMessage("Error: Absolute address exceeds machine size; "
          + "largest address used. ");
      addressField = MAX_SIZE - 1;
    }
    
    addressField += firstNumber * FIRST_DIGIT_MULTIPLIER;
    programText.setLinkedText(padLeft(addressField));
  }

  /**
   * @param module
   * @param programText
   * Relative address should be relocated by offset and checked that it does not exceed module size.
   */
  private void relocateRelative(Module module, ProgramText programText) {
    String address = programText.getUnlinkedText();
    int firstNumber = Character.getNumericValue(address.charAt(0));
    int addressField = Integer.parseInt(address.substring(1, LINKED_TEXT_SIZE));
    
    // Check that module size is not exceeded
    if (addressField >= module.getSize()) {
      programText.addErrorMessage("Error: Relative address exceeds module size; "
          + "largest module address used. ");
      addressField = module.getSize() - 1;
    }

    addressField += module.getOffset() + (firstNumber * FIRST_DIGIT_MULTIPLIER);
    programText.setLinkedText(padLeft(addressField));
  }

  /**
   * @param module
   * @param programText
   * External addresses should be resolved to the value in symbol table.
   */
  private void resolveExternal(Module module, ProgramText programText) {
    String address = programText.getUnlinkedText();
    int firstNumber = Character.getNumericValue(address.charAt(0));
    
    // Use last variable if multiple variables map to the same instruction
    ArrayList<String> variables = module.getUseToSymbol().get(programText.getIndex());
    if (variables.size() != 1) {
      programText.addErrorMessage("Error: Multiple variables used in instruction; "
          + "all but last ignored. ");
    }
    String lastVariable = variables.get(variables.size() - 1);
    
    // Log error messages if a symbol is used without definition
    int addressField = 0;
    if (!symbolTable.containsKey(lastVariable)) {
      programText.addErrorMessage("Error: " + lastVariable + " is not defined; 111 used.");
      addressField = DEFAULT_SYMBOL_LOCATION + (firstNumber * FIRST_DIGIT_MULTIPLIER);
    } else {     
      symbolTable.get(lastVariable).setUsed(true);
      addressField = symbolTable.get(lastVariable).getLocation() 
          + (firstNumber * FIRST_DIGIT_MULTIPLIER);
    }
    programText.setLinkedText(padLeft(addressField));
  }

  /**
   * @param number
   * @return
   * If the result is not 4 numbers, pad the left with 0's.
   */
  private String padLeft(int number) {
    String result = String.valueOf(number);
    while (result.length() < LINKED_TEXT_SIZE) {
      result = "0" + result;
    }
    return result;
  }

  /**
   * Log warnings if a symbol was defined but never used.
   */
  public void checkDefinitionUsage() {
    for (Entry<String, Symbol> entry : symbolTable.entrySet()) {
      if (!entry.getValue().isUsed()) {
        entry.getValue().addErrorMessage("Warning: " + entry.getKey() + " was defined in module " + 
            entry.getValue().getModuleNumber() + " but never used. ");
      }
    }
  }

  /**
   * @return errorMessage
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * @param errorMessage
   * Add error message
   */
  public void addErrorMessage(String errorMessage) {
    this.errorMessage += errorMessage;
  }

  /**
   * @return list of modules
   */
  public ArrayList<Module> getModuleList() {
    return moduleList;
  }

  /**
   * @param module
   * Add a module to list of modules
   */
  public void addModule(Module module) {
    moduleList.add(module);
  }
  
  /**
   * @return Symbol Table
   */
  public HashMap<String, Symbol> getSymbolTable() {
    return symbolTable;
  }
}
