package nl.utwente.group10.haskell.type;

import java.util.ArrayList;
import java.util.ListIterator;

public class TupleTypeCon extends TypeCon {

    /*
     * @param arity the number of arguments (>= 2) in the type
     */
    public TupleTypeCon(int arity) {
        super(tupleName(arity));
    }
    
    /*
     * @param arity the number of arguments (>= 2) in the type
     * @return the name of a tuple constructor 
     */
    public final static String tupleName(final int arity) {
        StringBuilder out = new StringBuilder();
        out.append('(');
        for (int n = 1; n < arity; n++) {
            out.append(',');
        }
        out.append('(');
        return out.toString();
    }

    @Override
    public String asTypeAppChain(final int fixity, final ArrayList<Type> args)
    {
        final StringBuilder out = new StringBuilder();
        out.append('(');
        final ListIterator<Type> iter = args.listIterator(args.size());
        while (iter.hasPrevious()) {
            out.append(iter.previous().toHaskellType(0));
            out.append(", ");
        }
        out.append(')');
        return out.toString();    
    }
}
