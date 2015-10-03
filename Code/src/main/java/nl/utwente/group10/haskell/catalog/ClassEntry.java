package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeClass;
import nl.utwente.group10.haskell.typeparser.TypeBuilder;

import java.util.Set;

/**
 * A type class in the Haskell catalog.
 */
public class ClassEntry extends Entry {
    /** The string representations of the instances of this type class. */
    private Set<String> instances;

    /**
     * @param name The name of this type class.
     * @param instances The string representations of the instances of this type class.
     */
    public ClassEntry(final String name, final Set<String> instances) {
        super(name);
        this.instances = instances;
    }

    /**
     * @return The string representations of the instances of this type class.
     */
    public final Set<String> getInstances() {
        return this.instances;
    }

    /**
     * Parses, constructs and returns the type class for this entry.
     * @param ctx The context to use.
     * @return The type class for this entry.
     */
    @Override
    public final TypeClass asHaskellObject(final Context ctx) {
        TypeBuilder builder = new TypeBuilder(ctx.typeClasses);
        TypeClass tc = new TypeClass(this.getName());

        for (String instance : this.instances) {
        	Type t = builder.build(instance);
        	if (t instanceof ConstT) {
        		tc.getTypes().add((ConstT) t);
        	}
        }

        return tc;
    }
}
