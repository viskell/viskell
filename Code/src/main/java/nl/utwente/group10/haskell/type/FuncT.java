package nl.utwente.group10.haskell.type;

import nl.utwente.group10.ghcj.HaskellException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class FuncT extends Type {
    /**
     * The argument types for this function type.
     */
    private final Type[] arguments;

    /**
     * @param arguments The argument types for this function type.
     */
    public FuncT(final Type... arguments) {
        super("Function");
        this.arguments = arguments.clone();
    }

    /**
     * @return The number of arguments for this function type, excluding the resulting type.
     */
    public final int getNumArgs() {
        return this.arguments.length - 1;
    }

    /**
     *
     * @param args Arguments to apply. The number of arguments should be less or equal to {@code this.getNumArgs()}.
     * @return The resulting type of the application.
     * @throws AssertionError
     * @throws HaskellException Invalid Haskell operation. See exception message for details.
     */
    public final Type getAppliedType(final Type ... args) throws HaskellException {
        assert args.length < this.arguments.length;

        final Map<Type, Type> types = new HashMap<Type, Type>();
        final Type[] resultArguments = new Type[this.arguments.length - args.length];

        // Determine resulting type of each argument and build resulting type
        int j = 0;
        for (int i = 0; i < this.arguments.length; i++) {
            if (i < args.length && !types.containsKey(this.arguments[i])) {
                // We determine the resulting type for this argument type and throw an exception when it is not valid.
                if (this.arguments[i].compatibleWith(args[i])) {
                    types.put(this.arguments[i], args[i]);
                } else {
                    throw new HaskellException(null); // TODO Improve this exception with a message
                }
            } else if (i >= args.length && types.containsKey(this.arguments[i])) {
                // We already determined the resulting type for this argument type, use this or throw an exception.
                if (this.arguments[i].compatibleWith(args[i])) {
                    resultArguments[j] = types.get(this.arguments[i]);
                    j++;
                } else {
                    throw new HaskellException(null); // TODO Improve this exception with a message
                }
            } else {
                // The resulting type for this argument type does not change.
                resultArguments[j] = this.arguments[i];
                j++;
            }
        }

        // Build type
        return resultArguments.length == 1 ? resultArguments[0] : new FuncT(resultArguments);
    }

    @Override
    public final boolean compatibleWith(Type other) {
        boolean compatible = true;

        if (other instanceof FuncT) {
            // If the other type is a {@code FuncT} instance, compare all overlapping subtypes.
            final Type[] otherArguments = ((FuncT) other).arguments;

            if (this.arguments.length >= otherArguments.length) {
                for (int i = 0; i < otherArguments.length && compatible; i++) {
                    compatible = this.arguments[i].compatibleWith(otherArguments[i]);
                }
            }
        } else if (this.arguments.length > 0) {
            // If not, compare the compatibility of the first argument.
            compatible = this.arguments[0].compatibleWith(other);
        } else {
            // If this {@code FuncT} does not have any arguments, the other type is per definition not compatible.
            compatible = false;
        }

        return compatible;
    }

    @Override
    public final String toHaskellType() {
        final StringBuilder out = new StringBuilder();
        out.append("(");


        for (int i = 0; i < this.arguments.length; i++) {
            out.append(this.arguments[i].toHaskellType());
            if (i + 1 < this.arguments.length) {
                out.append(" -> ");
            }
        }

        out.append(")");
        return out.toString();
    }

    @Override
    public final String toString() {
        return "FuncT{" +
                "arguments=" + Arrays.toString(this.arguments) +
                '}';
    }

    public final FuncT clone() {
        return new FuncT(this.arguments);
    }
}
