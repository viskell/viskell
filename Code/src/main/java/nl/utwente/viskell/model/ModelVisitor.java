package nl.utwente.viskell.model;

public abstract class ModelVisitor {
    public void visit(Component component) {
    }

    public void visit(ConstantBox constantBox) {
    }

    public void visit(EvalBox evalBox) {
    }

    public void visit(FunctionBox functionBox) {
    }

    public void visit(LambdaBox lambdaBox) {
    }

    public void visit(Wire wire) {
    }

    public void visit(SourcePort sourcePort) {
    }

    public void visit(SinkPort sinkPort) {
    }

    public void visit(Selection selection) {
    }
}
