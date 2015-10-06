package nl.utwente.group10.haskell.type;

import java.util.List;

public class ListTypeCon extends TypeCon {

    ListTypeCon() {
        super("[]");
    }

    @Override
    protected String asTypeAppChain(final int fixity, final List<Type> args)
    {
        if (args.size() != 1) {
            // for the not exact applied tuple constructor use the prefix notation
            return super.asTypeAppChain(fixity, args);
        }

        return "[" + args.get(0).prettyPrint(0) + "]";
    }
}
