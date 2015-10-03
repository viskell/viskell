package nl.utwente.group10.haskell.type;

import java.util.ArrayList;

public class ListTypeCon extends TypeCon {

    public ListTypeCon() {
        super("[]");
    }

    @Override
    public String asTypeAppChain(final int fixity, final ArrayList<Type> args)
    {
        if (args.size() != 1) {
            return super.asTypeAppChain(fixity, args);
        }

        return "[" + args.get(0).toHaskellType(0) + "]";
    }
}
