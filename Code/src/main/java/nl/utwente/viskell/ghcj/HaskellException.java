package nl.utwente.viskell.ghcj;

import nl.utwente.viskell.haskell.expr.Expression;

/**
 * Exception that is thrown when there is a problem with the Haskell code. This Exception can be thrown because of an
 * error in GHCi or because of a problem detected by the internal systems.
 */
public class HaskellException extends Exception {
    /**
     * Expression (subclass) instance that caused this Exception to be thrown. It can be {@code null}.
     */
    private final Expression exp;

    /**
     * Wrap another Exception in a HaskellException.
     * @param e The other exception.
     */
    public HaskellException(final Exception e) {
        super(e);
        this.exp = null;
    }

    /**
     * A HaskellException with a message and object that caused the Exception to be thrown.
     *
     * @param msg The message.
     * @param obj The object. May be {@code null}.
     */
    public HaskellException(final String msg, final Expression exp) {
        super(msg);
        this.exp = exp;
    }

    /**
     * A HaskellException with a message.
     *
     * @param msg The message.
     */
    public HaskellException(final String msg) {
        this(msg, null);
    }

    /**
     * Returns the Expression that caused this Exception to be thrown. Might return {@code null}.
     * @return An Expression instance or {@code null}.
     */
    public final Expression getExpression() {
        return this.exp;
    }

}
