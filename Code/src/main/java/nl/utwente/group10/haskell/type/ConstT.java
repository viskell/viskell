package nl.utwente.group10.haskell.type;

import com.google.common.base.Joiner;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Constant, concrete type. However, it may consist of variable types.
 */
public class ConstT extends ConcreteType implements Comparable<ConstT> {
    /**
     * The constructor for this type.
     */
    protected final String constructor;

    /**
     * The types of the arguments for this type.
     */
    protected final Type[] args;

    /**
     * @param constructor
     *            The constructor for this constant type.
     * @param args
     *            The types of the arguments that this type accepts.
     */
    public ConstT(final String constructor, final Type... args) {
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
    public String toHaskellType(final int fixity) {
        StringBuilder out = new StringBuilder();
        out.append(this.constructor);

        for (Type arg : this.args) {
            out.append(" ");
            out.append(arg.toHaskellType(10));
        }

        if (fixity > 9 && this.args.length > 0) {
            return "(" + out.toString() + ")";
        }

        return out.toString();
    }

    @Override
    protected ConstT getFreshInstance(IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh) {
        return new ConstT(this.constructor, this.getFreshArgs(staleToFresh));
    }

    @Override
    public boolean containsOccurenceOf(TypeVar tvar) {
        for (Type t : this.args) {
            if (t.containsOccurenceOf(tvar)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return An array of fresh arguments.
     */
    protected Type[] getFreshArgs(IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh) {
        List<Type> fresh = new LinkedList<>();

        for (Type arg : this.args) {
            fresh.add(arg.getFreshInstance(staleToFresh));
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

    public int compareTo(final ConstT type) {
        if (type == null) {
            throw new NullPointerException();
        } else if (type instanceof ConstT) {
            final ConstT cType = (ConstT) type;
            if (this.constructor.equals(cType.constructor)) {
                if (this.args.length == cType.args.length) {
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
