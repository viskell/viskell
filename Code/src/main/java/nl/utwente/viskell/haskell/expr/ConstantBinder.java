package nl.utwente.viskell.haskell.expr;

/** Represents a value within a pattern match. */
public class ConstantBinder extends Binder {

    /** Haskell representation of the value. */
    private final String value;
    
    public ConstantBinder(String value) {
        super("constant");
        this.value = value;
    }

    @Override
    public String getUniqueName() {
        // FIXME: we should not abuse this method for code generation
        return value;
    }
    
}
