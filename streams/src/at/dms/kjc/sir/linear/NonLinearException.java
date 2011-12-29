package at.dms.kjc.sir.linear;

/**
 * This exception is thrown when a filter is determined to be non-linear.
 * The exception mechanism is used to short circuit the analysis of filters
 * once we determine that they are non-linear.
 **/
public class NonLinearException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7130835326862439102L;

	public NonLinearException(String m) {
        super(m);
    }
}

