package nl.utwente.group10.haskell.exceptions;

import nl.utwente.group10.haskell.HaskellObject;

/**
 * Exception that is thrown when a Haskell type error is detected.
 */
public class HaskellTypeError extends HaskellException {
    /**
     * @param msg A user-readable message providing more information.
     * @param obj The HaskellObject that caused the error.
     */
    public HaskellTypeError(final String msg, final HaskellObject obj) {
        super(msg, obj);
    }

    /**
     * @param msg A user-readable message providing more information.
     */
    public HaskellTypeError(final String msg) {
        super(msg);
    }
}
