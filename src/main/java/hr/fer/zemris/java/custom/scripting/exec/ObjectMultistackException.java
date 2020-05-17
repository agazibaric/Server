package hr.fer.zemris.java.custom.scripting.exec;

/**
 * Exception used in {@link ObjectMultistack} class when unallowed situation happens.
 * 
 * @author Ante Gazibarić
 * @version 1.0
 *
 */
public class ObjectMultistackException extends RuntimeException {
	
	/** Default serial key */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for creating new {@code ObjectMultistackException} object.
	 * 
	 * @param message message that describes invalid event that happened 
	 */
	public ObjectMultistackException(String message) {
		super(message);
	}

}
