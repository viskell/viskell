package nl.utwente.group10.haskell.exceptions;

import nl.utwente.group10.haskell.HaskellObject;
import nl.utwente.group10.haskell.type.Type;

import com.google.common.base.Optional;

/**
 * Exception that is thrown when a Haskell type error is detected.
 */
public class HaskellTypeError extends HaskellException {
    /**
     * The first type that caused this error.
     */
    private final Optional<Type> t1;

    /**
     * The second type that caused this error.
     */
    private final Optional<Type> t2;

    /**
     * @param msg A user-readable message providing more information.
     * @param obj The HaskellObject that caused the error.
     */
    public HaskellTypeError(final String msg, final HaskellObject obj) {
        this(msg, obj, null, null);
    }

    /**
     * @param msg A user-readable message providing more information.
     * @param obj The HaskellObject that caused the error.
     * @param t1 The first type that caused this error.
     * @param t2 The second type that caused this error.
     */
    public HaskellTypeError(final String msg, final HaskellObject obj, final Type t1, final Type t2) {
        super(msg, obj);
        this.t1 = Optional.fromNullable(t1);
        this.t2 = Optional.fromNullable(t2);
    }

    /**
     * @param msg A user-readable message providing more information.
     */
    public HaskellTypeError(final String msg) {
        this(msg, null, null, null);
    }

    /**
     * @return An Optional of the first type related to this type error.
     */
    public final Optional<Type> getOptionalFirstType() {
        return this.t1;
    }

    /**
     * @return An Optional of the second type related to this type error.
     */
    public final Optional<Type> getOptionalSecondType() {
        return this.t2;
    }

    /**
     * @return The type string of the first type related to this error or the empty string if there is no type set.
     */
    public final String getFirstHaskellType() {
        return this.t1.isPresent() ? this.t1.get().toHaskellType() : "";
    }

    /**
     * @return The type string of the second type related to this error or the empty string if there is no type set.
     */
    public final String getSecondHaskellType() {
        return this.t2.isPresent() ? this.t2.get().toHaskellType() : "";
    }
}
