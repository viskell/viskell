package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FunctionTest {
    private Env env;

    @Before
    public void setUp() throws CatalogException {
        this.env = new HaskellCatalog().asEnvironment();
    }

    @Test
    public void testArguments() {
        Function f = new Function(new Ident("pi"));
        assertEquals(0, f.getArguments().length);

        Function.FunctionArgument arg0 = f.addArgument();
        Function.FunctionArgument arg1 = f.addArgument();

        assertEquals("arg0", arg0.toHaskell());
        assertEquals(1, arg1.getPosition());

        assertEquals(2, f.getArguments().length);
        assertEquals(arg0, f.getArguments()[0]);
        assertEquals(arg1, f.getArguments()[1]);

        f.removeArgument(arg0);

        assertEquals(1, f.getArguments().length);
        assertEquals(arg1, f.getArguments()[0]);
    }

    @Test
    public void testToHaskell() {
        Function f = new Function(new Ident("pi"));
        Function.FunctionArgument arg0 = f.addArgument();
        Function.FunctionArgument arg1 = f.addArgument();

        assertEquals(String.format("\\ %s %s = pi", arg0.toHaskell(), arg1.toHaskell()), f.toHaskell());
    }

    @Test
    public void testAnalyze() throws HaskellException {
        Function add5 = new Function();
        Expr add5Expr = new Apply(new Ident("(+)"), add5.addArgument());
        add5.setExpr(add5Expr);

        assertEquals("\\ arg0 = ((+) arg0)", add5.toHaskell());

        Expr addition = new Apply(add5, new Value(new ConstT("Int"), "5"));

        assertEquals("Int", addition.analyze(this.env).prune().toHaskellType());
    }
}