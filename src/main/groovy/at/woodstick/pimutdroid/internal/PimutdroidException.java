package at.woodstick.pimutdroid.internal;

public class PimutdroidException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PimutdroidException() {
        super();
    }

    public PimutdroidException(String message) {
        super(message);
    }
    
    public PimutdroidException(String message, Throwable cause) {
        super(message, cause);
    }

    public PimutdroidException(Throwable cause) {
        super(cause);
    }
	
}
