package nl.utwente.group10.haskell.type;

import com.google.common.base.Joiner;

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
     */
    public final Type getAppliedType(final Type ... args) {
        assert args.length < this.arguments.length;

        final Map<Type, Type> types = new HashMap<Type, Type>();
        final Type[] resultArguments = new Type[this.arguments.length - args.length];

        // Determine resulting type of each argument and build resulting type
        for (int i = 0; i < this.arguments.length; i++) {
            if (i < args.length && !types.containsKey(this.arguments[i])) {
                // We determine the resulting type for this argument type
                types.put(this.arguments[i], args[i]);
                resultArguments[i] = args[i];
            } else if (types.containsKey(this.arguments[i])) {
                // We already determined the resulting type for this argument type
                resultArguments[i] = types.get(this.arguments[i]);
            } else {
                // The resulting type for this argument type does not change.
                resultArguments[i] = this.arguments[i];
            }
        }

        // Build type
        return resultArguments.length == 1 ? resultArguments[0] : new FuncT(resultArguments);
    }

    @Override
    public final String toHaskell() {
        return "(" + Joiner.on(" -> ").join(this.arguments) + ")";
    }

    @Override
    public final String toString() {
        return "FuncT{" +
                "arguments=" + Arrays.toString(this.arguments) +
                '}';
    }
}
