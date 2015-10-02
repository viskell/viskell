package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.typeparser.TypeBuilder;

/**
 * A function entry in the Haskell catalog.
 */
public class FunctionEntry extends Entry {
    /** The category this Entry belongs to. */
    private final String category;

    /** The signature of this Entry. */
    private final String signature;

    /** The documentation string for this Entry. */
    private final String documentation;

    /**
     * @param name The function name of this Entry.
     * @param category The category this Entry belongs to.
     * @param signature The signature of this Entry.
     * @param documentation The documentation for this Entry.
     */
    FunctionEntry(final String name, final String category, final String signature, final String documentation) {
        super(name);
        this.category = category;
        this.signature = signature;
        this.documentation = documentation;
    }

    /**
     * @return The category of this function.
     */
    public final String getCategory() {
        return this.category;
    }

    /**
     * @return The signature of this function.
     */
    public final String getSignature() {
        return this.signature;
    }

    /**
     * @return The documentation of this function.
     */
    public final String getDocumentation() {
        return this.documentation;
    }

    /**
     * Parses, constructs and returns the type of this function.
     * @param ctx The context to use.
     * @return The type of this function.
     */
    @Override
    public Type asHaskellObject(Context ctx) {
        TypeBuilder builder = new TypeBuilder(ctx.typeClasses);
        return builder.build(this.getSignature());
    }
}
