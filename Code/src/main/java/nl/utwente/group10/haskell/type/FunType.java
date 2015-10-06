package nl.utwente.group10.haskell.type;

/**
 * Type of a Haskell function.
 */
public class FunType extends ConcreteType {

    /**
     * The argument type that this function type accepts.
     */
    private final Type argument;

    /**
     * The result type that this function type returns.
     */
    private final Type result;

    /**
     * @param arg The argument type that this function type accepts.
     * @param result The result type that this function type returns.
     */
    public FunType(final Type arg, final Type res) {
        this.argument = arg;
        this.result = res;
    }

    public Type getArgument() {
        return argument;
    }

    public Type getResult() {
        return result;
    }

    @Override
    public final String toHaskellType(final int fixity) {
        final StringBuilder out = new StringBuilder();

        out.append(this.argument.toHaskellType(1));
        out.append(" -> ");
        out.append(this.result.toHaskellType(0));

        if (fixity > 0) {
            return "(" + out.toString() + ")";
        }

        return out.toString();
    }

    @Override
    public FunType getFresh(TypeScope scope) {
        return new FunType(this.argument.getFresh(scope), this.result.getFresh(scope));
    }

    @Override
    public boolean containsOccurenceOf(TypeVar tvar) {
        return this.argument.containsOccurenceOf(tvar) || this.result.containsOccurenceOf(tvar);
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", argument, result);
    }
}
