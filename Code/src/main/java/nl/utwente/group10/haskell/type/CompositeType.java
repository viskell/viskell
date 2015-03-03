package nl.utwente.group10.haskell.type;

import java.util.Map;

/**
 * Abstract class for types that consist of other types.
 */
public abstract class CompositeType extends Type {
    /**
     * @return A new type based on this type with the variable types made more concrete by the entries in the given map.
     */
    public abstract CompositeType getResolvedType(Map<VarT, Type> types);
}
