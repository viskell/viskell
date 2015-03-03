package nl.utwente.group10.haskell.exceptions;

import nl.utwente.group10.ghcj.GhciException;

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

    /**
     * A HaskellException with a message.
     *
     * @param msg The message.
     */
    public HaskellException(String msg) {
        super(msg);
    }
}
