package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.FuncT;

/**
 * Abstract class for expressions that always have the {@code FuncT} type.
 */
public abstract class Func extends Expr {
    /**
     * @param type The type of this function.
     */
    protected Func(FuncT type) {
        super(type);
    }
}
