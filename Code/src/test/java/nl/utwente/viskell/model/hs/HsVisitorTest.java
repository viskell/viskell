package nl.utwente.viskell.model.hs;

import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.model.Component;
import nl.utwente.viskell.model.ConstantBox;
import nl.utwente.viskell.model.FunctionBox;
import nl.utwente.viskell.model.Wire;
import org.junit.Test;

import static org.junit.Assert.*;

public class HsVisitorTest {
    @Test
    public void testSimple() throws HaskellTypeError {
        Component component = new Component();
        Type x = Type.con("x");
        Type i = Type.con("Int");
        Expression id = new Value(new FunType(x, x), "id");
        Expression ten = new Value(i, "10");
        ConstantBox idb = new ConstantBox(component, "constant_id", id, id.inferType());
        ConstantBox tb = new ConstantBox(component, "constant_ten", ten, ten.inferType());
        FunctionBox fb = new FunctionBox(component);

        Wire.connect(idb.getOutputs().get(0), fb.getInputs().get(0));
        Wire.connect(tb.getOutputs().get(0), fb.getInputs().get(1));

        HsVisitor hsVisitor = new HsVisitor();
        fb.accept(hsVisitor);
        String result = hsVisitor.toString();

        assertNotNull(result);
        assertNotEquals("", result);

        System.out.println(result);
    }

}