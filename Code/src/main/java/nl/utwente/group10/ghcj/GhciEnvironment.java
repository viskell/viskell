package nl.utwente.group10.ghcj;

import nl.utwente.group10.haskell.expr.Binding;
import java.util.List;

public class GhciEnvironment {
    private GhciSession ghci;

    public GhciEnvironment(GhciSession ghci) {
        this.ghci = ghci;
    }

    public List<Binding> getBindings() throws GhciException {
        System.out.println(ghci.eval(":browse"));
        return null;
    }
}
