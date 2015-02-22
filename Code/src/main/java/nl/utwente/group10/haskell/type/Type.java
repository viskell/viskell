package nl.utwente.group10.haskell.type;

import nl.utwente.group10.haskell.HaskellObject;

/**
 * Abstract class for Haskell types. Provides an interface for common methods.
 */
public abstract class Type extends HaskellObject implements Cloneable {
    /**
     * The name of this type.
     */
    private final String name;

    /**
     * @param name The name of this type.
     */
    protected Type(String name) {
        this.name = name;
    }

    /**
     * @return The name of this type.
     */
    public final String getName() {
        return this.name;
    }

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
    public final boolean equals(Object other) {
        return this.hashCode() == other.hashCode();
    }

    @Override
    public abstract String toString();
}
