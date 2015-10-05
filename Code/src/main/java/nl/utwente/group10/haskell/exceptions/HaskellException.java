package nl.utwente.group10.haskell.exceptions;

import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.haskell.HaskellObject;
import nl.utwente.group10.haskell.expr.Expression;
import nl.utwente.group10.haskell.type.Type;

import com.google.common.base.Optional;

/**
 * Exception that is thrown when there is a problem with the Haskell code. This Exception can be thrown because of an
 * error in GHCi or because of a problem detected by the internal systems.
 */
public class HaskellException extends GhciException {
    /**
     * HaskellObject (subclass) instance that caused this Exception to be thrown. Represented as an Optional because it
     * can be {@code null}.
     */
    private final Optional<HaskellObject> obj;

    /**
     * Wrap another Exception in a HaskellException.
     * @param e The other exception.
     */
    public HaskellException(final Exception e) {
        super(e);
        this.obj = Optional.absent();
    }

    /**
     * A HaskellException with a message and object that caused the Exception to be thrown.
     *
     * @param msg The message.
     * @param obj The object. May be {@code null}.
     */
    public HaskellException(final String msg, final HaskellObject obj) {
        super(msg);
        this.obj = Optional.fromNullable(obj);
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
     * @return Whether this exception contains a HaskellObject instance.
     */
    public final boolean hasHaskellObject() {
        return this.obj.isPresent();
    }

    /**
     * Returns the HaskellObject that caused this Exception to be thrown. Might return {@code null}. Use of
     * {@code getOptionalHaskellObject()} is encouraged.
     * @return An HaskellObject instance or {@code null}.
     */
    public final HaskellObject getHaskellObject() {
        return this.obj.get();
    }

    /**
     * Returns an instance of Guava's Optional that may contain a HaskellObject that caused this Exception to be thrown.
     * @return The Optional instance that may contain the HaskellObject.
     */
    public final Optional<HaskellObject> getOptionalHaskellObject() {
        return this.obj;
    }

    /**
     * Returns the Haskell string representation for the HaskellObject instance or the empty string if there is no
     * object present.
     * @return Haskell string representation or the empty string.
     */
    public final String getObjectRepresentation() {
        String out = "";

        if (this.obj.isPresent()) {
            if (this.obj.get() instanceof Expression) {
                out = ((Expression) this.obj.get()).toHaskell();
            } else if (this.obj.get() instanceof Type) {
                out = ((Type) this.obj.get()).toHaskellType();
            }
        }

        return out;
    }
}
