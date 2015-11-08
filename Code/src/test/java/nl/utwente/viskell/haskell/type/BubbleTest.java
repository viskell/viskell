package nl.utwente.viskell.haskell.type;

import nl.utwente.viskell.haskell.expr.Apply;
import nl.utwente.viskell.haskell.expr.Value;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BubbleTest {
    @Test
    public void testBubbleUnit() throws Exception {
        TypeScope scope = new TypeScope();

        TypeVar a = scope.getVar("a");
        TypeVar z = scope.getVar("z");
        TypeCon u = Type.con("Unit");

        Type aFunc = Type.fun(a, a);
        Type bFunc = Type.fun(u, u);

        // "(unit (id undefined))"
        Apply apply = new Apply(new Value(bFunc, "unit"), new Apply(new Value(aFunc, "id"), new Value(z, "undefined")));

        // Do type inference.
        Type t = apply.inferType();

        // Inferred type of the whole expression is 'Unit'.
        assertEquals("Unit", t.prettyPrint());

        // Inferred type of "(unit (id undefined))" is 'Unit'.
        assertEquals("Unit", u.prettyPrint());

        // Inferred type of "(id undefined)" is 'Unit'.
        assertEquals("Unit", a.prettyPrint());

        // Inferred type of (this instance of the 'value') "undefined" is 'Unit'.
        assertEquals("Unit", z.prettyPrint());
    }

    @Test
    public void testBubbleAddition() throws Exception {
        TypeScope scope = new TypeScope();
        TypeVar a = scope.getVar("a");
        TypeVar b = scope.getVar("b");
        TypeVar c = scope.getVar("c");
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

        Type t = apply.inferType();

        assertEquals("Float", t.prettyPrint());
        assertEquals("Float", a.prettyPrint());
        assertEquals("Float", b.prettyPrint());
        assertEquals("Float", c.prettyPrint());
    }

    @Test
    public void testBubbleEquals() throws Exception {
        TypeScope scope = new TypeScope();
        TypeCon Float = Type.con("Float");
        TypeClass Num = new TypeClass("Num", Float);
        TypeVar a = scope.getVarTC("a", Num);
        TypeVar b = scope.getVar("b");

        Apply apply = new Apply(
                new Apply(
                        new Value(Type.fun(a, a, a), "=="),
                        new Value(Float, "5.0")
                ),
                new Value(b, "undefined")
        );

        assertEquals("(((==) (5.0)) (undefined))", apply.toHaskell());

        Type t = apply.inferType();

        assertEquals("Float", t.prettyPrint());
        assertEquals("Float", a.prettyPrint());
        assertEquals("Float", b.prettyPrint());
    }
}
