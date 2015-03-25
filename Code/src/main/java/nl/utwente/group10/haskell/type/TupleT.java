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
        return "(" + Joiner.on(", ").join(this.getArgs()) + ")";
    }
}
