package nl.utwente.group10.haskell.type;

import nl.utwente.group10.haskell.HaskellObject;

/**
 *
 */
public abstract class Type extends HaskellObject {
    /**
     * @return The Haskell representation of this type.
     */
    public abstract String toHaskell();

    /**
     * @return A string representation of this Haskell type.
     */
    public abstract String toString();
}
