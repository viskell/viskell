package nl.utwente.group10.haskell.type;

import nl.utwente.group10.haskell.HaskellObject;

/**
 * Abstract class for Haskell types. Provides an interface for common methods.
 *
 * Types should be immutable. Also, a Type subclass instance that represents the same in Haskell as another instance of
 * the same class should be equal, that is {@code this.equals(that)} should return {@code true}.
 */
public abstract class Type extends HaskellObject implements Comparable<Type> {
    /**
     * Tests whether the given type is usable within the context of this type.
     *
     * Examples:
     * - The `Integer` type is valid within the type class `Num`
     * - Every type is valid within a {@code VarT} type.
     *
     * @param other The type to test.
     * @return Whether the given type is usable in place of this type.
     */
    public abstract boolean compatibleWith(Type other);

    /**
     * @return The Haskell representation of this type.
     */
    public abstract String toHaskellType();

    @Override
    public final int hashCode() {
        return this.toHaskellType().hashCode();
    }

    @Override
    public final boolean equals(final Object other) {
        return this.getClass() == other.getClass() && this.hashCode() == other.hashCode();
    }

    @Override
    public abstract String toString();

    @Override
    public int compareTo(final Type other) {
        return this.toHaskellType().compareTo(other.toHaskellType());
    }
}
