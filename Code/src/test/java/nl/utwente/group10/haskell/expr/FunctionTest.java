package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.env.HaskellCatalog;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.Type;
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
        Function.FunctionArgument arg0 = new Function.FunctionArgument(Type.var("a", env.lookupClass("Num")));
        Function.FunctionArgument arg1 = new Function.FunctionArgument(Type.var("a", env.lookupClass("Num")));

        Function f = new Function(new Ident("pi"), arg0, arg1);

        assertEquals(2, f.getArguments().length);
        assertEquals(arg0, f.getArguments()[0]);
        assertEquals(arg1, f.getArguments()[1]);
    }

    @Test
    public void testToHaskell() {
        Function.FunctionArgument arg = new Function.FunctionArgument(Type.var("a", env.lookupClass("Num")));
        Expr applies = new Apply(new Ident("(+)"), arg);
        Function f = new Function(applies, arg);

        assertEquals(String.format("(\\ %1$s -> ((+) %1$s))", arg.toHaskell()), f.toHaskell());
    }

    @Test
    public void testAnalyze() throws HaskellException {
        Function.FunctionArgument arg = new Function.FunctionArgument(Type.con("Int"));
        Expr applies = new Apply(new Ident("(+)"), arg);
        Function f = new Function(applies, arg);

        assertEquals("Int -> Int -> Int", f.analyze(this.env).toHaskellType());

        Function.FunctionArgument arg1 = new Function.FunctionArgument(Type.con("Int"));
        Function add5 = new Function(new Apply(new Apply(f, new Value(Type.con("Int"), "5")), arg1), arg1);

        assertEquals("Int -> Int", add5.analyze(this.env).toHaskellType());
    }
}