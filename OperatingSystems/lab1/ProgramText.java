
/**
 * @author Connie Shi (cs3313@nyu.edu)
 * Program text represents one program text in the input file, storing both unlinked 
 * and linked text.
 */
public class ProgramText {
	/**
	 * Address types represented as enum, where 
	 * IMMEDIATE = 1, ABSOLUTE = 2, RELATIVE = 3, and EXTERNAL = 4
	 * ERROR is used for error catching invalid inputs.
	 */
	enum AddressType {
		IMMEDIATE,
		ABSOLUTE,
		RELATIVE,
		EXTERNAL,
		ERROR;
	}

	private static final int UNLINKED_TEXT_LENGTH = 5;
	private String unlinkedText;
	private String linkedText;
	private AddressType type;
	private String errorMessage = "";
	private int index;

	public ProgramText(String unlinkedText, int index) {
		if (unlinkedText.length() == UNLINKED_TEXT_LENGTH) {
			this.unlinkedText = unlinkedText;
		} else {
		  System.out.println("FATAL ERROR: " + unlinkedText + " does not have length 5. EXITING...");
		  System.exit(1);
		}
		this.index = index;
		type = convertType(unlinkedText);
	}

	/**
	 * @param text
	 * @return
	 * Returns the address type of text depending on the last digit.
	 */
	private AddressType convertType(String text) {
		int lastDigit = Character.getNumericValue(text.charAt(UNLINKED_TEXT_LENGTH - 1));
		switch (lastDigit) {
		case 1: return AddressType.IMMEDIATE;
		case 2: return AddressType.ABSOLUTE;
		case 3: return AddressType.RELATIVE;
		case 4: return AddressType.EXTERNAL;
		default: System.out.println("Text does not end in 1, 2, 3, or 4. Text = " + text);
		  return AddressType.ERROR;
		}
	}

	/**
	 * @return unlinked text
	 */
	public String getUnlinkedText() {
		return unlinkedText;
	}

	/**
	 * @return error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 * Adds error message to existing error message String
	 */
	public void addErrorMessage(String errorMessage) {
		this.errorMessage += errorMessage;
	}

	/**
	 * @return address type
	 */
	public AddressType getType() {
		return type;
	}

  /**
   * @return linked text
   */
  public String getLinkedText() {
    return linkedText;
  }

  /**
   * @param linkedText
   * Sets linked text.
   */
  public void setLinkedText(String linkedText) {
    this.linkedText = linkedText;
  }

  /**
   * @return index position of program text in overall program
   */
  public int getIndex() {
    return index;
  }
}
