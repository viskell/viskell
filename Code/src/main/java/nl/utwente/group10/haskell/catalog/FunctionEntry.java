package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.type.Type;

/**
 * A function entry in the Haskell catalog.
 */
public class FunctionEntry  implements Comparable<FunctionEntry> {
    /** The name of this entry. */
    private String name;
    
    /** The category this Entry belongs to. */
    private final String category;

    /** The type signature of this Entry. */
    private final Type signature;

    /** The documentation string for this Entry. */
    private final String documentation;

    /**
     * @param name The function name of this Entry.
     * @param category The category this Entry belongs to.
     * @param signature The signature of this Entry.
     * @param documentation The documentation for this Entry.
     */
    FunctionEntry(final String name, final String category, final Type signature, final String documentation) {
        this.name = name;
        this.category = category;
        this.signature = signature;
        this.documentation = documentation;
    }

    /**
     * @return The name of this function.
     */
    public final String getName() {
        return this.name;
    }
    
    /**
     * @return The category of this function.
     */
    public final String getCategory() {
        return this.category;
    }

    /**
     * @return The a fresh copy of type signature of this function.
     */
    public final Type getSignature() {
        return this.signature.getFresh();
    }

    /**
     * @return The documentation of this function.
     */
    public final String getDocumentation() {
        return this.documentation;
    }

    @Override
    public final int compareTo(final FunctionEntry entry) {
        return this.getName().compareTo(entry.getName());
    }

}
