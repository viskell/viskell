package nl.utwente.viskell.model.graphviz;

import nl.utwente.viskell.model.*;

public class Graphvizitor extends ModelVisitor {
    private StringBuilder sb;

    private static final String PROLOG = "digraph G {\n  node[shape=record];\n";
    private static final String EPILOG = "}\n";

    public Graphvizitor() {
        sb = new StringBuilder();
    }

    public String toString() {
        String result = sb.toString();
        sb.setLength(0); // reset for reuse
        return PROLOG + result + EPILOG;
    }

    private void visitBox(Box box) {
        sb.append("\n")
          .append("  subgraph cluster_")
          .append(box.hashCode())
          .append(" {\n")
          .append("    style=filled;\n")
          .append("    node [style=filled,color=white];\n")
          .append("    color=lightgrey;\n")
          .append("    label=\"")
          .append(box.toString())
          .append("\";\n");

        for (InputPort input : box.getInputs()) {
            sb.append("    in_").append(input.hashCode()).append(";\n");
        }

        for (OutputPort output : box.getOutputs()) {
            sb.append("    out_").append(output.hashCode()).append(";\n");
        }

        sb.append("  }\n")
          .append("\n");
    }

    @Override
    public void visit(Wire wire) {
        sb.append("  out_")
          .append(wire.getSource().hashCode())
          .append(" -> in_")
          .append(wire.getSink().hashCode())
          .append(";\n");
    }

    // Use visitBox as visit implementation for all Box subclasses
    @Override public void visit(ConstantBox constantBox) { visitBox(constantBox); }
    @Override public void visit(EvalBox evalBox)         { visitBox(evalBox); }
    @Override public void visit(FunctionBox functionBox) { visitBox(functionBox); }
    @Override public void visit(LambdaBox lambdaBox)     { visitBox(lambdaBox); }
}
