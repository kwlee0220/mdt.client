package mdt.client;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class InternalException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InternalException(String details, Throwable cause) {
		super(details, cause);
	}
	
	public InternalException(String details) {
		super(details);
	}
	
	public InternalException(Throwable cause) {
		super(cause);
	}
}
