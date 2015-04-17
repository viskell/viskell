package nl.utwente.group10.haskell.type;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TypeClassTest {

    private final ConstT integer = new ConstT("Int");
    private final ConstT floating = new ConstT("Float");
    private final ConstT doubl = new ConstT("Double");
    private ConstT string = new ConstT("String");

    @Test
    public final void intersectTest() {
        final TypeClass num = new TypeClass("Num", integer, floating, doubl);
        final TypeClass weird = new TypeClass("Weird", integer, string);

        assertEquals("[Int]", TypeClass.intersect(num, weird).toString());
    }
}
