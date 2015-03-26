package nl.utwente.group10.haskell.type;

import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.Map;

/**
 * Tuple type.
 */
public class TupleT extends ConstT {
    /**
     * @param args The types of the elements for this tuple.
     */
    public TupleT(Type... args) {
        super("(,)", args);
    }

    @Override
    public final String toHaskellType() {
        StringBuilder out = new StringBuilder();
        Type[] args = this.getArgs();

        out.append("(");

        for (int i = 0; i < args.length; i++) {
            out.append(args[i].toHaskellType());

            if (i + 1 < args.length) {
                out.append(", ");
            }
        }

        out.append(")");
        return out.toString();
    }
}
