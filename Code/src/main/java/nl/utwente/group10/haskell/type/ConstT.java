package nl.utwente.group10.haskell.type;

import com.google.common.base.Joiner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Constant, concrete type. However, it may consist of variable types.
 */
public class ConstT extends Type {
    /**
     * The constructor for this type.
     */
    protected final String constructor;

    /**
     * The types of the arguments for this type.
     */
    protected final Type[] args;

    /**
     * @param constructor The constructor for this constant type.
     * @param args The types of the arguments that this type accepts.
     */
    public ConstT(final String constructor, final Type ... args) {
        this.constructor = constructor;
        this.args = args;
    }

    /**
     * @return The constructor for this type.
     */
    public final String getConstructor() {
        return this.constructor;
    }

    /**
     * @return The types of the arguments for this type.
     */
    public final Type[] getArgs() {
        return this.args;
    }

    @Override
    public final Type prune() {
        for (int i = 0; i < this.args.length; i++) {
            this.args[i] = this.args[i].prune();
        }

        return this;
    }

    @Override
    public String toHaskellType() {
        StringBuilder out = new StringBuilder();
        out.append(this.constructor);

        for (Type arg : this.args) {
            out.append(" ");
            out.append(arg.toHaskellType());
        }

        return out.toString();
    }

    @Override
    public ConstT getFresh() {
        return new ConstT(this.constructor, this.getFreshArgs());
    }

    /**
     * Returns an array of fresh arguments. Selectively calls {@code getFresh} on each argument. When the same Type
     * instance appears multiple times in the arguments no new type is instantiated. Instead, the fresh type is reused.
     * @return An array of fresh arguments.
     */
    protected Type[] getFreshArgs() {
        List<Type> fresh = new LinkedList<>();

        for (Type arg : this.args) {
            if (fresh.contains(arg)) {
                fresh.add(fresh.get(fresh.indexOf(arg)));
            } else {
                fresh.add(arg.getFresh());
            }
        }

        return fresh.toArray(new Type[fresh.size()]);
    }

    @Override
    public String toString() {
        if (this.args.length > 0) {
            return String.format("(%s %s)", this.constructor, Joiner.on(' ').join(this.args));
        } else {
            return this.constructor;
        }
    }

    @Override
    public int compareTo(final Type type) {
        if (type == null) {
            throw new NullPointerException();
        } else if (type instanceof ConstT) {
            final ConstT cType = (ConstT) type;
            if (this.constructor.equals(cType.constructor)) {
                if (this.args.length == cType.args.length) {
                    for (int i = 0; i < this.args.length; i++) {
                        if (this.args[i].compareTo(cType.args[i]) != 0) {
                            return -1;
                        }
                    }
                    return 0;
                } else {
                    return -1;
                }
            } else {
                return this.constructor.compareTo(cType.constructor);
            }
        } else {
            return -1;
        }
    }
}
