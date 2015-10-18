package nl.utwente.viskell.haskell.type;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.util.List;

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
        return String.format("(%s)", Strings.repeat(",", arity - 1));
    }

    @Override
    protected String prettyPrintAppChain(final int fixity, final List<Type> args) {
        if (this.name.length() > args.size() + 2) {
            // for the partial applied tuple constructor use the prefix notation
            super.prettyPrintAppChain(fixity, args);
        }
        
        return "(" + Joiner.on(", ").join(args.stream().map(a -> a.prettyPrint(0)).iterator()) + ")";
    }
}
