package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.HaskellObject;

/**
 * Abstract class for catalog entry types.
 */
public abstract class Entry implements Comparable<Entry> {
    /** The name of this entry. */
    private String name;

    /**
     * @param name The name of this entry.
     */
    protected Entry(final String name) {
        this.name = name;
    }

    /**
     * @return The name of this entry.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Constructs and returns the HaskellObject (Type, Expr) for this entry.
     * @param ctx The context to use.
     * @return The HaskellObject for this entry.
     */
    public abstract HaskellObject asHaskellObject(final Context ctx);

    @Override
    public final int compareTo(final Entry entry) {
        return this.getName().compareTo(entry.getName());
    }
}
