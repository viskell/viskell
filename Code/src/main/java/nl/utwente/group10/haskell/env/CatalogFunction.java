package nl.utwente.group10.haskell.env;

import nl.utwente.group10.haskell.type.Type;

/**
 * A function entry in the Haskell catalog.
 */
public class CatalogFunction extends FunctionInfo implements Comparable<CatalogFunction> {
    /** The category this function belongs to. */
    private final String category;

    /** The documentation string for this Entry. */
    private final String documentation;

    /**
     * @param name The function name.
     * @param category The category this function belongs to.
     * @param signature The type signature for this function.
     * @param documentation The documentation for this function.
     */
    CatalogFunction(final String name, final String category, final Type signature, final String documentation) {
        super(name, signature);
        this.category = category;
        this.documentation = documentation;
    }

    /**
     * @return The category of this function.
     */
    public final String getCategory() {
        return this.category;
    }

    /**
     * @return The documentation of this function.
     */
    public final String getDocumentation() {
        return this.documentation;
    }

    @Override
    public final int compareTo(final CatalogFunction entry) {
        return this.getName().compareTo(entry.getName());
    }

}
