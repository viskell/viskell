package nl.utwente.group10.haskell.type;

import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.List;

/**
 * Haskell type class to be interpreted as type. The {@code compatibleWith} makes sure that all types supported by a
 * type class can be used in the place where that {@code TypeClass} instance is used.
 */
public class TypeClass extends Type {
    /**
     * A list of the types within this type class.
     */
    private final List<Type> types;

    /**
     * @param name The name of this type class.
     * @param types The types in this type class.
     */
    public TypeClass(final String name, Type ... types) {
        super(name);
        this.types = Arrays.asList(types);
    }

    /**
     * Adds a type to this type class.
     * @param type The type to add.
     */
    public final void addType(Type type) {
        this.types.add(type);
    }

    @Override
    public final boolean compatibleWith(Type other) {
        return this.types.contains(other);
    }

    @Override
    public final String toHaskellType() {
        return Joiner.on("\n" + this.getName() + " ").join(this.types);
    }

    @Override
    public final String toString() {
        return "TypeClass{" +
                "name='" + this.getName() + "'" +
                "types=" + types.toArray().toString() +
                '}';
    }

    public TypeClass clone() {
        return new TypeClass(this.getName(), (Type[]) this.types.toArray());
    }
}
