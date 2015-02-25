package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.Type;

/**
 * Value in Haskell. Haskell values are always defined as String in Java. The responsibility of inputting a valid value,
 * e.g. wrapping a String in quotes, is the responsibility of the user.
 */
public class Value extends Expr {
    /**
     * Haskell representation of the value.
     */
    private String value;

    /**
     * @param type Type of this value.
     * @param value Haskell representation of the value that is valid for the given type.
     */
    protected Value(final Type type, final String value) {
        super(type);
        this.value = value;
    }

    /**
     * @param type Type of this value.
     */
    protected Value(final Type type) {
        this(type, "");
    }

    /**
     * @return Haskell representation of the value.
     */
    public final String getValue() {
        return this.value;
    }

    /**
     * @param value Haskell representation of the new value.
     */
    public final void setValue(final String value) {
        this.value = value;
    }

    @Override
    public final String toHaskell() {
        return this.value;
    }

    @Override
    public final String toString() {
        return "Value{" +
                "type=" + this.getType() +
                ", value='" + this.value + '\'' +
                '}';
    }
}
