package nl.utwente.group10.haskell.type;

import nl.utwente.group10.haskell.HaskellObject;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Type of a Haskell function. Consists of multiple types in a fixed order.
 */
public class FuncT extends CompositeType {
    /**
     * The argument types for this function type.
     */
    private final Type[] arguments;

    /**
     * @param arguments The argument types for this function type.
     */
    public FuncT(final Type... arguments) {
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
     * @param relatedObj The HaskellObject that the applied type is calculated for. Only used in case of an Exception.
     *                   May be {@code null}.
     * @param args Arguments to apply. The number of arguments should be less or equal to {@code this.getNumArgs()}.
     * @return The resulting type of the application.
     * @throws AssertionError
     * @throws HaskellTypeError Invalid Haskell operation. See exception message for details.
     */
    public final Type getAppliedType(final HaskellObject relatedObj, final Type ... args) throws HaskellTypeError {
        assert args.length < this.arguments.length;

        final Map<VarT, Type> varTypes = new HashMap<VarT, Type>();
        final Type[] resultArguments = new Type[this.arguments.length - args.length];

        // Determine resulting type of each argument and build resulting type
        int j = 0;
        for (int i = 0; i < this.arguments.length; i++) {
            Type expectedType = this.arguments[i]; // Expected type

            // Check whether the expected type is a VarT instance, if so, do some extra processing
            if (expectedType instanceof VarT) {
                if (varTypes.containsKey(expectedType)) {
                    // If we already determined the real type of the VarT, set the expected type accordingly.
                    expectedType = varTypes.get(expectedType);
                } else if (i < args.length) {
                    // If this is the first occurance of this variable, set its actual type but keep the VarT as
                    // expected type to check compatibility later on.
                    varTypes.put((VarT) expectedType, args[i]);
                }
            }

            // Check whether the type that will be applied is compatible with the expected type. The expected type can
            // be a VarT or (a VarT replaced by) a non-variable type.
            if (i < args.length && !expectedType.compatibleWith(args[i])) {
                throw new HaskellTypeError(
                        String.format(
                                "The given type '%s' is not compatible with the expected type '%s' for argument %d.",
                                args[i].toHaskellType(), expectedType.toHaskellType(), i
                        ),
                        relatedObj
                );
            } else if (i >= args.length) {
                resultArguments[j] = expectedType;
                j++;
            }
        }

        // Build type
        return resultArguments.length == 1 ? resultArguments[0] : new FuncT(resultArguments);
    }

    @Override
    public final FuncT getResolvedType(final Map<VarT, Type> types) {
        final Type[] arguments = new Type[this.arguments.length];

        for (int i = 0; i < this.arguments.length; i++) {
            if (this.arguments[i] instanceof CompositeType) {
                arguments[i] = ((CompositeType) this.arguments[i]).getResolvedType(types);
            } else if (this.arguments[i] instanceof VarT && types.containsKey(this.arguments[i])) {
                arguments[i] = types.get(this.arguments[i]);
            } else {
                arguments[i] = this.arguments[i];
            }
        }

        return new FuncT(arguments);
    }

    @Override
    public final boolean compatibleWith(final Type other) {
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
}
