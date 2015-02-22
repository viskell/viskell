package nl.utwente.group10.ghcj;

/**
 * Exception that is thrown when Ghci balks on our input.
 */
public class HaskellException extends GhciException {
    /**
     * Wrap another Exception in a HaskellException.
     * @param e The other exception.
     */
    public HaskellException(final Exception e) {
        super(e);
    }

    /** A HaskellException with a message */
    public HaskellException(String msg) {
        super(msg);
    }
}
