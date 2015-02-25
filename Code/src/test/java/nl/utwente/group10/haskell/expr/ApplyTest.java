package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.ghcj.HaskellException;
import nl.utwente.group10.haskell.type.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplyTest {
    private Type T_integer;
    private Type T_float;
    private Type T_string;
    private Type T_numeric;
    private Type T_a;
    private Type T_b;
    private Type T_c;

    @Before
    public void setUp() {
        // Some primitive types
        this.T_integer = new ConstT("Integer");
        this.T_float = new ConstT("Floating");
        this.T_string = new ConstT("String");

        // A typeclass
        this.T_numeric = new TypeClass("Num", T_integer, T_float);

        // Two variable types
        this.T_a = new VarT("a", T_numeric);
        this.T_b = new VarT("b", T_numeric);
        this.T_c = new VarT("c", T_numeric, T_string);
    }

    @Test
    public void testSimpleType() throws HaskellException {
        Func add = new EnvFunc("(+)", new FuncT(T_integer, T_integer, T_integer));
        Apply a = new Apply(add, new Value(T_integer, "4"), new Value(T_integer, "6"));

        assertEquals(a.toHaskell(), "(+) 4 6");
        assertEquals(a.getType(), T_integer);
    }

//    @Test
    public void testCompositeType() throws HaskellException {
        Func massive = new EnvFunc("massive", new FuncT(T_a, T_b, T_c, new TupleT(T_a, T_b, T_c), T_string));

        // First application
        Apply a1 = new Apply(massive, new Value(T_float, "1.2"), new Value(T_integer, "0"), new Value(T_string, "\"a\""));
        assertEquals(a1.getType().toHaskellType(), new FuncT(new TupleT(T_integer, T_float, T_string), T_string).toHaskellType());
        assertEquals(a1.toHaskell(), "massive 1.2 0 \"a\"");

        // Second application
        Apply a2 = new Apply(a1, new Value(new TupleT(T_a, T_b, T_c), "(4.5, 3, \"b\")"));
        assertEquals(a2.getType(), T_string);
        assertEquals(a2.toHaskell(), "massive 1.2 0 \"a\" (4.5, 3, \"b\")");

        // Third application
        Apply a3 = new Apply(massive, new Value(T_float, "1.2"), new Value(T_integer, "0"),
                new Value(T_string, "\"a\""), new Value(new TupleT(T_a, T_b, T_c), "(4.5, 3, \"b\")"));
        assertEquals(a3.getType(), a2.getType());
        assertEquals(a3.toHaskell(), a2.toHaskell());
    }
}
