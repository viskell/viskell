package nl.utwente.group10.haskell.type;

import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Value;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BubbleTest {
    @Test
    public void testBubbleUnit() throws Exception {
        TypeVar a = Type.var("a");
        TypeVar z = Type.var("z");
        TypeCon u = Type.con("Unit");

        Type aFunc = Type.fun(a, a);
        Type bFunc = Type.fun(u, u);

        // "(unit (id undefined))"
        Apply apply = new Apply(new Value(bFunc, "unit"), new Apply(new Value(aFunc, "id"), new Value(z, "undefined")));

        // Do type inference.
        Type t = apply.findType(new Environment());

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
        TypeCon floatT = Type.con("Float");

        Apply apply = new Apply(
                new Apply(
                        new Value(Type.fun(floatT, floatT, floatT), "+"),
                        new Apply(
                                new Value(Type.fun(a, a), "id"),
                                new Value(b, "undefined")
                        )
                ),
                new Value(c, "undefined")
        );

        assertEquals("(((+) ((id) (undefined))) (undefined))", apply.toHaskell());

        Type t = apply.findType(new Environment());

        assertEquals("Float", t.toHaskellType());
        assertEquals("Float", a.toHaskellType());
        assertEquals("Float", b.toHaskellType());
        assertEquals("Float", c.toHaskellType());
    }

    @Test
    public void testBubbleEquals() throws Exception {
        TypeCon Float = Type.con("Float");
        TypeClass Num = new TypeClass("Num", Float);
        TypeVar a = Type.var("a", Num);
        TypeVar b = Type.var("b");

        Apply apply = new Apply(
                new Apply(
                        new Value(Type.fun(a, a, a), "=="),
                        new Value(Float, "5.0")
                ),
                new Value(b, "undefined")
        );

        assertEquals("(((==) (5.0)) (undefined))", apply.toHaskell());

        Type t = apply.findType(new Environment());

        assertEquals("Float", t.toHaskellType());
        assertEquals("Float", a.toHaskellType());
        assertEquals("Float", b.toHaskellType());
    }
}
