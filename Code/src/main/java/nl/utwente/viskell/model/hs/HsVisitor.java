package nl.utwente.viskell.model.hs;

import nl.utwente.viskell.haskell.expr.Hole;
import nl.utwente.viskell.haskell.expr.Variable;
import nl.utwente.viskell.model.ConstantBox;
import nl.utwente.viskell.model.FunctionBox;
import nl.utwente.viskell.model.ModelVisitor;

public class HsVisitor extends ModelVisitor {
    private StringBuilder sb;

    private final static String PROLOG = "let { ";
    private final static String EPILOG = "} in ()";

    public HsVisitor() {
        sb = new StringBuilder();
    }

    @Override
    public String toString() {
        return PROLOG + sb.toString() + EPILOG;
    }

    @Override
    public void visit(FunctionBox functionBox) {
        sb.append(functionBox.getOutputs().get(0).getVariable().toHaskell());
        sb.append(" =");

        functionBox.getInputs().forEach(in -> {
            sb.append(" ");
            Variable v = in.getOppositePort().map(src -> src.getVariable()).orElse(new Hole());
            sb.append(v.toHaskell());
        });

        sb.append("; ");
    }

    @Override
    public void visit(ConstantBox constantBox) {
        sb.append(constantBox.getOutputs().get(0).getVariable().toHaskell());
        sb.append(" = ");
        sb.append(constantBox.getExpr().toHaskell());
        sb.append("; ");
    }
}
