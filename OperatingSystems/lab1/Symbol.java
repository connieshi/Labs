
/**
 * @author Connie Shi (cs3313@nyu.edu)
 * Symbol in Symbol Table with name, location, and if it was used in program.
 */
public class Symbol {
    private String name;
    private int location;
    private boolean isUsed;
    private int moduleNumber;
    private String errorMessage = "";
    
    public Symbol(String name, int location, int moduleNumber) {
      this.name = name;
      this.location = location;
      this.moduleNumber = moduleNumber;
    }

    /**
     * @return returns name
     */
    public String getName() {
      return name;
    }

    /**
     * @return location of the symbol
     */
    public int getLocation() {
      return location;
    }

    /**
     * @param location
     * Sets location of symbol
     */
    public void setLocation(int location) {
      this.location = location;
    }

    /**
     * @return if used in program
     */
    public boolean isUsed() {
      return isUsed;
    }

    /**
     * @param isUsed
     * Sets if symbol was used in program
     */
    public void setUsed(boolean isUsed) {
      this.isUsed = isUsed;
    }
    
    /**
     * @param errorMessage
     * Add an error message in existing error message String
     */
    public void addErrorMessage(String errorMessage) {
      this.errorMessage += errorMessage;
    }
    
    /**
     * @return error message
     */
    public String getErrorMessage() {
      return errorMessage;
    }

    /**
     * @return module number that symbol was used
     */
    public int getModuleNumber() {
      return moduleNumber;
    }
  }