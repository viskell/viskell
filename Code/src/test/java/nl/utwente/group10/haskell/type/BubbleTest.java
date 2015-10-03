package nl.utwente.group10.haskell.type;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Value;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BubbleTest {
    @Test
    public void testBubbleUnit() throws Exception {
        VarT a = new VarT("a");
        VarT z = new VarT("z");
        ConstT u = new ConstT("Unit");

        FuncT aFunc = new FuncT(a, a);
        FuncT bFunc = new FuncT(u, u);

        // "(unit (id undefined))"
        Apply apply = new Apply(new Value(bFunc, "unit"), new Apply(new Value(aFunc, "id"), new Value(z, "undefined")));

        // Do type inference.
        Type t = apply.analyze(new Env());

        // Inferred type of the whole expression is 'Unit'.
        assertEquals("Unit", t.prune().toHaskellType());

        // Inferred type of "(unit (id undefined))" is 'Unit'.
        assertEquals("Unit", u.prune().toHaskellType());

        // Inferred type of "(id undefined)" is 'Unit'.
        assertEquals("Unit", a.prune().toHaskellType());

        // Inferred type of (this instance of the 'value') "undefined" is 'Unit'.
        assertEquals("Unit", z.prune().toHaskellType());
    }

    @Test
    public void testBubbleAddition() throws Exception {
        VarT a = TypeChecker.makeVariable("a");
        VarT b = TypeChecker.makeVariable("b");
        VarT c = TypeChecker.makeVariable("c");
        ConstT floatT = new ConstT("Float");

        Apply apply = new Apply(
                new Apply(
                        new Value(new FuncT(floatT, new FuncT(floatT, floatT)), "+"),
                        new Apply(
                                new Value(new FuncT(a, a), "id"),
                                new Value(b, "undefined")
                        )
                ),
                new Value(c, "undefined")
        );

        assertEquals("(((+) ((id) (undefined))) (undefined))", apply.toHaskell());

        Type t = apply.analyze(new Env());

        assertEquals("Float", t.prune().toHaskellType());
        assertEquals("Float", a.prune().toHaskellType());
        assertEquals("Float", b.prune().toHaskellType());
        assertEquals("Float", c.prune().toHaskellType());
    }

    @Test
    public void testBubbleEquals() throws Exception {
        ConstT Float = new ConstT("Float");
        TypeClass Num = new TypeClass("Num", Float);
        VarT a = new VarT("a", Num);
        VarT b = new VarT("b");

        Apply apply = new Apply(
                new Apply(
                        new Value(new FuncT(a, new FuncT(a, a)), "=="),
                        new Value(Float, "5.0")
                ),
                new Value(b, "undefined")
        );

        assertEquals("(((==) (5.0)) (undefined))", apply.toHaskell());

        Type t = apply.analyze(new Env());

        assertEquals("Float", t.prune().toHaskellType());
        assertEquals("Float", a.prune().toHaskellType());
        assertEquals("Float", b.prune().toHaskellType());
    }
}
