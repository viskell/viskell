package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BindingTest {
    @Test
    public void testToHaskell() {
        Binding b = new Binding("ten", new Value(new ConstT("Integer"), "10"));

        assertEquals(b.getName(), "ten");
        assertEquals(b.getExpr().getType(), new ConstT("Integer"));
        assertEquals(((Value) b.getExpr()).getValue(), "10");
    }
}
