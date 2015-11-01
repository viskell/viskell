package nl.utwente.viskell.haskell.type;

import nl.utwente.viskell.ghcj.HaskellException;


/**
 * Exception that is thrown when a Haskell type error is detected.
 */
public class HaskellTypeError extends HaskellException {

    /**
     * @param msg A user-readable message providing more information.
     */
    public HaskellTypeError(final String msg) {
        super(msg);
    }


}
