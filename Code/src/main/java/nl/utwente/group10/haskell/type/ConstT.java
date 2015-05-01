package nl.utwente.group10.haskell.type;

import com.google.common.base.Joiner;
import com.sun.org.apache.xpath.internal.functions.FuncFalse;

/**
 * Constant, concrete type. However, it may consist of variable types.
 */
public class ConstT extends Type {
    /**
     * The constructor for this type.
     */
    private final String constructor;

    /**
     * The types of the arguments for this type.
     */
    private final Type[] args;

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
    public String toString() {
        if (this.args.length > 0) {
            return String.format("(%s %s)", this.constructor, Joiner.on(' ').join(this.args));
        } else {
            return this.constructor;
        }
    }

    @Override
    public int compareTo(final Type type) {
        if (type instanceof ConstT) {
            if (this.constructor.compareTo(((ConstT) type).constructor) == 0) {
                if (this.args.length == ((ConstT) type).args.length) {
                    for (int i = 0; i < this.args.length; i++) {
                        if (this.args[i].compareTo(((ConstT) type).args[i]) != 0) {
                            return -1;
                        }
                    }
                    return 0;
                } else {
                    return -1;
                }
            } else {
                return this.constructor.compareTo(((ConstT) type).constructor);
            }
        } else {
            return -1;
        }
    }
}
