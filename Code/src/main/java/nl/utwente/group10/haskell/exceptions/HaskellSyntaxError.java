package nl.utwente.group10.haskell.exceptions;

import nl.utwente.group10.haskell.HaskellObject;

/**
 * Exception that is thrown when a Haskell syntax error is detected.
 */
public class HaskellSyntaxError extends HaskellException {
    /**
     * @param msg A user-readable message providing more information.
     * @param obj The HaskellObject that caused the error.
     */
    public HaskellSyntaxError(String msg, HaskellObject obj) {
        super(msg, obj);
    }

    /**
     * @param msg A user-readable message providing more information.
     */
    public HaskellSyntaxError(String msg) {
        super(msg);
    }
}
