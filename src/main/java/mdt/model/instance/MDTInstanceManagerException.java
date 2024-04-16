package mdt.model.instance;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTInstanceManagerException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MDTInstanceManagerException(String details, Throwable cause) {
		super(details, cause);
	}
	
	public MDTInstanceManagerException(String details) {
		super(details);
	}
	
	public MDTInstanceManagerException(Throwable cause) {
		super(cause);
	}
}
