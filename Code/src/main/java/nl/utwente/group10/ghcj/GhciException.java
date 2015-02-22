package nl.utwente.group10.ghcj;

/**
 * Any failure in communicating with Ghci or executing user input. Superclass
 * for other, more specific exceptions.
 */
public class GhciException extends Exception {
    /**
     * Wrap another Exception in a GhciException.
     * @param e The other exception.
     */
    public GhciException(final Exception e) {
        super(e);
    }

    /** A GhciException with a message */
    public GhciException(String msg) {
        super(msg);
    }
}
