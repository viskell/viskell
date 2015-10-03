package nl.utwente.group10.haskell.type;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Value;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BubbleTest {
    @Test
    public void testBubbleUnit() throws Exception {
        TypeVar a = new TypeVar("a");
        TypeVar z = new TypeVar("z");
        ConstT u = new ConstT("Unit");

        FunType aFunc = new FunType(a, a);
        FunType bFunc = new FunType(u, u);

        // "(unit (id undefined))"
        Apply apply = new Apply(new Value(bFunc, "unit"), new Apply(new Value(aFunc, "id"), new Value(z, "undefined")));

        // Do type inference.
        Type t = apply.analyze(new Env());

        // Inferred type of the whole expression is 'Unit'.
        assertEquals("Unit", t.toHaskellType());

        // Inferred type of "(unit (id undefined))" is 'Unit'.
        assertEquals("Unit", u.toHaskellType());

        // Inferred type of "(id undefined)" is 'Unit'.
        assertEquals("Unit", a.toHaskellType());

        // Inferred type of (this instance of the 'value') "undefined" is 'Unit'.
        assertEquals("Unit", z.toHaskellType());
    }

    @Test
    public void testBubbleAddition() throws Exception {
        TypeVar a = TypeChecker.makeVariable("a");
        TypeVar b = TypeChecker.makeVariable("b");
        TypeVar c = TypeChecker.makeVariable("c");
        ConstT floatT = new ConstT("Float");

        Apply apply = new Apply(
                new Apply(
                        new Value(new FunType(floatT, new FunType(floatT, floatT)), "+"),
                        new Apply(
                                new Value(new FunType(a, a), "id"),
                                new Value(b, "undefined")
                        )
                ),
                new Value(c, "undefined")
        );

        assertEquals("(((+) ((id) (undefined))) (undefined))", apply.toHaskell());

        Type t = apply.analyze(new Env());

        assertEquals("Float", t.toHaskellType());
        assertEquals("Float", a.toHaskellType());
        assertEquals("Float", b.toHaskellType());
        assertEquals("Float", c.toHaskellType());
    }

    @Test
    public void testBubbleEquals() throws Exception {
        ConstT Float = new ConstT("Float");
        TypeClass Num = new TypeClass("Num", Float);
        TypeVar a = new TypeVar("a", Num);
        TypeVar b = new TypeVar("b");

        Apply apply = new Apply(
                new Apply(
                        new Value(new FunType(a, new FunType(a, a)), "=="),
                        new Value(Float, "5.0")
                ),
                new Value(b, "undefined")
        );

        assertEquals("(((==) (5.0)) (undefined))", apply.toHaskell());

        Type t = apply.analyze(new Env());

        assertEquals("Float", t.toHaskellType());
        assertEquals("Float", a.toHaskellType());
        assertEquals("Float", b.toHaskellType());
    }
}
