package nl.utwente.viskell.haskell.expr;

/*
 * Anything that looks like using a variable in Haskell code. 
 */
public abstract class Variable extends Expression {

    /**
     * Name of this variable.
     */
    protected final String name;

    /**
     * @param name of this variable.
     */
    public Variable(String name) {
        this.name = name;
    }

    @Override
    public String toHaskell() {
        return this.name;
    }

}