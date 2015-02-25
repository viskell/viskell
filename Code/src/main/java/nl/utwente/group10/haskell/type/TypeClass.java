package nl.utwente.group10.haskell.type;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

/**
 * Haskell type class to be interpreted as type. The {@code compatibleWith} makes sure that all types supported by a
 * type class can be used in the place where that {@code TypeClass} instance is used.
 */
public class TypeClass extends Type {
    /**
     * The Haskell name of this type class.
     */
    private final String name;

    /**
     * A list of the types within this type class.
     */
    private final ImmutableList<Type> types;

    /**
     * @param name The Haskell name of this type class.
     * @param types The types in this type class. The provided array will be sorted. No element in the provided array
     *              should be a {@code VarT} or {@code TypeClass} instance.
     */
    public TypeClass(final String name, final Type ... types) {
        this.name = name;
        Arrays.sort(types);
        this.types = ImmutableList.copyOf(types);
    }

    public final List<Type> getTypes() {
        return this.types;
    }

    @Override
    public final boolean compatibleWith(final Type other) {
        return this.types.contains(other);
    }

    @Override
    public final String toHaskellType() {
        final StringBuilder out = new StringBuilder();

        for (final Type type : this.types) {
            out.append("instance ").append(this.name).append(" ").append(type.toHaskellType())
                    .append(System.lineSeparator());
        }

        return out.toString().trim();
    }

    @Override
    public final String toString() {
        return "TypeClass{" +
                "name='" + this.name + "'" +
                "types=" + Arrays.toString(this.types.toArray()) +
                '}';
    }
}
