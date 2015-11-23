import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * @author Connie Shi (cs3313@nyu.edu)
 * Operating System Lab 1: Linker Lab
 * 
 * Given object modules as inputs: symbol definition, use list, and program text, 
 * generate the resulting linked memory mappings. Note that if a symbol exceeds 8 characters, 
 * only the first 8 characters will be represent that particular symbol. 
 */
public class LinkerLab {
  
  // Maximum length of a symbol
  private static final int MAX_SYMBOL_LENGTH = 8;

  public static void main(String[] args) {
    Linker linker = new Linker();
    parseInput(linker);
    linker.getResultAddresses();
    linker.checkDefinitionUsage();

    // Print symbol table and error messages
    System.out.println("Symbol Table");
    for (Entry<String, Symbol> entry : linker.getSymbolTable().entrySet()) {
      System.out.printf("%8s = %3d %s\n", entry.getKey(), entry.getValue().getLocation(),
          entry.getValue().getErrorMessage());
    }

    // Print memory mapping
    System.out.println();
    System.out.println("Memory Map");
    for (Module module : linker.getModuleList()) {
      for (ProgramText programText : module.getProgramText()) {
        System.out.printf("%8d: %s %s\n", programText.getIndex(), programText.getLinkedText(),
            programText.getErrorMessage());
      }
    }

    // Print module error messages
    for (Module module : linker.getModuleList()) {
      if (!module.getErrorMessage().isEmpty()) {
        System.out.println(module.getErrorMessage());
      }
    }
    
    // Print linker error messages
    System.out.println(linker.getErrorMessage());
  }

  /**
   * @param linker
   * Parses VALID input file or print "Invalid input format" if the input file is invalid.
   */
  private static void parseInput(Linker linker) {
    Scanner scanner = new Scanner(System.in);
    int moduleNumber = 0;
    int offset = 0;

    try {
      while (scanner.hasNext()) {
        // Stores symbols used in current module to check symbol definition
        ArrayList<Symbol> currentModuleSymbols = new ArrayList<Symbol>(); 

        // Read inputs and populate objects
        readDefinitionList(scanner, linker, currentModuleSymbols, offset, moduleNumber);
        ArrayList<String> useList = readUseList(scanner);
        String[] programTextString = readProgramText(scanner);

        // Check symbol definitions and create module
        checkSymbolDefinitions(currentModuleSymbols, offset, programTextString.length);
        Module module = new Module(moduleNumber, programTextString, offset, useList);
        linker.addModule(module);

        // Add offset to use next module
        offset += programTextString.length;
        moduleNumber++;
      }
    } catch (Exception e) {
      System.out.println("Invalid input format.");
      System.exit(1);
    }
    scanner.close();
  }
  
  /**
   * @param scanner
   * @param linker
   * @param currentModuleSymbols
   * @param offset
   * @param moduleNumber
   * Reads the definition list of symbol definitions into symbol table and save in 
   * currentModuleSymbols the symbols defined in current module, to be used later to verify
   * that symbols are not defined out of module size.
   */
  private static void readDefinitionList(Scanner scanner, Linker linker, 
      ArrayList<Symbol> currentModuleSymbols, int offset, int moduleNumber) {
    
    if (scanner.hasNextInt()) {
      int numDefinition = scanner.nextInt();
      for (int i = 0; i < numDefinition; i++) {
        String var = scanner.next();
        if (var.length() > MAX_SYMBOL_LENGTH ) {
          var = var.substring(0, MAX_SYMBOL_LENGTH);
        }
        int location = scanner.nextInt();
        Symbol symbol = linker.addSymbol(var, location + offset, moduleNumber);
        currentModuleSymbols.add(symbol);
      }
    }
  }
  
  /**
   * @param scanner
   * @return use list parsed into a list of Strings
   * Reads use list separated by white space and returns as a list of Strings
   */
  private static ArrayList<String> readUseList(Scanner scanner) {
    ArrayList<String> useList = new ArrayList<String>();
    if (scanner.hasNextInt()) {
      int numUses = scanner.nextInt();
      if (numUses > 0) {
        useList.add(String.valueOf(numUses));
      }
      for (int i = 0; i < numUses; i++) {
        String var = scanner.next();
        useList.add(var);
        int current;
        while ((current = scanner.nextInt()) != -1) {
          useList.add(String.valueOf(current));
        }
        useList.add(String.valueOf(-1));
      }
    }
    return useList;
  }
  
  /**
   * @param scanner
   * @return String[] of unlinked program text
   * Reads unlinked program text into array and returns the list of Strings
   */
  private static String[] readProgramText(Scanner scanner) {
    String[] programTextString = null;
    int programSize = 0;
    if (scanner.hasNextInt()) {
      programSize = scanner.nextInt();
      programTextString = new String[programSize];
      for (int i = 0; i < programSize; i++) {
        programTextString[i] = scanner.next();
      }
    }
    return programTextString;
  }

  /**
   * @param currentModuleSymbols
   * @param offset
   * @param programSize
   * Checks that symbols are defined within its module size otherwise use the last word in module.
   */
  private static void checkSymbolDefinitions(ArrayList<Symbol> currentModuleSymbols, int offset, 
      int programSize) {

    for (Symbol symbol : currentModuleSymbols) {
      if (symbol.getLocation() - offset >= programSize) {
        symbol.addErrorMessage("Error: Definition exceeds module size; "
            + "last word in module used. ");
        symbol.setLocation(offset + programSize - 1);
      }
    }
  }
}
