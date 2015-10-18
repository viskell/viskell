package nl.utwente.viskell.haskell.expr;

import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FunctionTest {
    private Environment env;
    private TypeScope scope;

    @Before
    public void setUp() {
        this.env = new HaskellCatalog().asEnvironment();
        this.scope = new TypeScope();
    }

    @Test
    public void testArguments() throws HaskellException {
        Function.FunctionArgument arg0 = new Function.FunctionArgument(this.scope.getVarTC("a", env.testLookupClass("Num")));
        Function.FunctionArgument arg1 = new Function.FunctionArgument(this.scope.getVarTC("a", env.testLookupClass("Num")));

        Function f = new Function(this.env.useFun("pi"), arg0, arg1);

        assertEquals(2, f.getArguments().length);
        assertEquals(arg0, f.getArguments()[0]);
        assertEquals(arg1, f.getArguments()[1]);
    }

    @Test
    public void testToHaskell() throws HaskellException {
        Function.FunctionArgument arg = new Function.FunctionArgument(this.scope.getVarTC("a", env.testLookupClass("Num")));
        Expression applies = new Apply(this.env.useFun("(+)"), arg);
        Function f = new Function(applies, arg);

        assertEquals(String.format("(\\ %1$s -> ((+) %1$s))", arg.toHaskell()), f.toHaskell());
    }

    @Test
    public void testAnalyze() throws HaskellException {
        Function.FunctionArgument arg = new Function.FunctionArgument(Type.con("Int"));
        Expression applies = new Apply(this.env.useFun("(+)"), arg);
        Function f = new Function(applies, arg);

        assertEquals("Int -> Int -> Int", f.findType().prettyPrint());

        Function.FunctionArgument arg1 = new Function.FunctionArgument(Type.con("Int"));
        Function add5 = new Function(new Apply(new Apply(f, new Value(Type.con("Int"), "5")), arg1), arg1);

        assertEquals("Int -> Int", add5.findType().prettyPrint());
    }
}