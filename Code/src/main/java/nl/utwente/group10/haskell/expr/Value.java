package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.Type;

/**
 * Value in Haskell. Haskell values are always defined as String in Java.
 */
public class Value extends Expr {
    /**
     * Haskell representation of the value.
     */
    private String val;

    /**
     * @param type Type of this value.
     * @param val Haskell representation of the value.
     */
    protected Value(final Type type, final String val) {
        super(type);
        this.val = val;
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
    public final String getVal() {
        return this.val;
    }

    /**
     * @param val Haskell representation of the new value.
     */
    public final void setVal(final String val) {
        this.val = val;
    }

    @Override
    public final String toHaskell() {
        return this.val;
    }

    @Override
    public final String toString() {
        return "Value{" +
                "type=" + this.getType() +
                ", val='" + this.val + '\'' +
                '}';
    }
}
