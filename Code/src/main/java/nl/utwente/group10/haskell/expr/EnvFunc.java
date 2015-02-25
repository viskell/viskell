package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.FuncT;

/**
 * Haskell function that is known to the Haskell environment. Used to call a function that is available in GHCi.
 */
public class EnvFunc extends Func {
    /**
     * The Haskell name of this environment function.
     */
    private final String name;

    /**
     * @param name The Haskell name of this builtin function.
     * @param type The type of this builtin function.
     */
    public EnvFunc(final FuncT type, final String name) {
        super(type);
        this.name = name;
    }

    @Override
    public final String toHaskell() {
        return this.name;
    }

    @Override
    public final String toString() {
        return "Builtin{" +
                "type=" + this.getType() +
                ", name='" + name + '\'' +
                '}';
    }
}
