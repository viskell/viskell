package nl.utwente.group10.haskell.type;

import java.util.List;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class TupleTypeCon extends TypeCon {

    /*
     * @param arity the number of arguments (>= 2) in the type
     */
    TupleTypeCon(int arity) {
        super(tupleName(arity));
    }
    
    /*
     * @param arity the number of arguments (>= 2) in the type
     * @return the name of a tuple constructor 
     */
    public final static String tupleName(final int arity) {
        return String.format("(%s)", Strings.repeat(",", arity));
    }

    @Override
    protected String asTypeAppChain(final int fixity, final List<Type> args) {
        if (this.name.length() > args.size() + 2) {
            // for the partial applied tuple constructor use the prefix notation
            super.asTypeAppChain(fixity, args);
        }
        
        Stream<String> parts = Lists.reverse(args).stream().map(e -> e.toHaskellType(0));
        return "(" + Joiner.on(", ").join(parts.iterator()) + ")";
    }
}
