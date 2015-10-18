package nl.utwente.viskell.haskell.type;

import java.util.List;

public class ListTypeCon extends TypeCon {

    ListTypeCon() {
        super("[]");
    }

    @Override
    protected String prettyPrintAppChain(final int fixity, final List<Type> args)
    {
        if (args.size() != 1) {
            // for the not exact applied tuple constructor use the prefix notation
            return super.prettyPrintAppChain(fixity, args);
        }

        return "[" + args.get(0).prettyPrint(0) + "]";
    }
}
