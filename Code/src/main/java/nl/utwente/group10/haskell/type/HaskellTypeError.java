package nl.utwente.group10.haskell.type;

import nl.utwente.group10.ghcj.HaskellException;
import nl.utwente.group10.haskell.expr.Expression;


/**
 * Exception that is thrown when a Haskell type error is detected.
 */
public class HaskellTypeError extends HaskellException {

    /**
     * @param msg A user-readable message providing more information.
     * @param exp The Expression that caused the error.
     */
    public HaskellTypeError(final String msg, final Expression exp) {
        super(msg, exp);
        if (exp == null) {
            throw new RuntimeException("HaskellTypeError lacking Expression\n" + msg);
        }
    }


}
