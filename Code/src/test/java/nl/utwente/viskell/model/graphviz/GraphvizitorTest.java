package nl.utwente.viskell.model.graphviz;

import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.model.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class GraphvizitorTest {
    @Test
    public void testSimple() throws HaskellTypeError {
        Component component = new Component();
        Type x = Type.con("x");
        Type i = Type.con("Int");
        Expression id = new Value(new FunType(x, x), "id");
        Expression ten = new Value(i, "10");
        ConstantBox idb = new ConstantBox(component, "id", id, id.inferType());
        ConstantBox tb = new ConstantBox(component, "10", ten, ten.inferType());
        FunctionBox fb = new FunctionBox(component);

        Wire.connect(idb.getOutputs().get(0), fb.getInputs().get(0));
        Wire.connect(tb.getOutputs().get(0), fb.getInputs().get(1));

        Graphvizitor graphvizitor = new Graphvizitor();
        fb.accept(graphvizitor);
        String result = graphvizitor.toString();

        assertNotNull(result);
        assertNotEquals("", result);
    }

}