package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.typeparser.TypeBuilder;

/** A function entry in the Haskell catalog. */
public class Entry implements Comparable<Entry> {
    /** The name of this Entry. */
    private final String name;

    /** The category this Entry belongs to. */
    private final String category;

    /** The signature of this Entry. */
    private final String signature;

    /** The documentation string for this Entry. */
    private final String documentation;
    
    /** The block type of this Entry. */
    private final String blockType;

    /**
     * Creates a new Entry instance.
     * @param name The function name of this Entry.
     * @param category The category this Entry belongs to.
     * @param signature The signature of this Entry.
     * @param documentation The documentation for this Entry.
     */
    Entry(final String name, final String category, final String signature, final String documentation, final String blockType) {
        this.name = name;
        this.category = category;
        this.signature = signature;
        this.documentation = documentation;
        this.blockType = blockType;
    }

    /**
     * @return The function name of this Entry.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @return The category this Entry belongs to.
     */
    public final String getCategory() {
        return this.category;
    }

    /**
     * @return The signature of this Entry.
     */
    public final String getSignature() {
        return this.signature;
    }

    /**
     * @return The documentation of this Entry.
     */
    public final String getDocumentation() {
        return this.documentation;
    }
    
    /**
     * @return The block type of this Entry.
     */
    public final String getBlockType() {
        return this.blockType;
    }

    /**
     * Parses and returns the Type of the function in this Entry.
     * @return The Type of this Entry.
     */
    public final Type getType() {
        TypeBuilder builder = new TypeBuilder();
        return builder.build(this.getSignature());
    }

    @Override
    public int compareTo(Entry entry) {
        return this.getName().compareTo(entry.getName());
    }
}
