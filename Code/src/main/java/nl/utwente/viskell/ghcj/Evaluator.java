package nl.utwente.viskell.ghcj;

abstract public class Evaluator {
    public abstract String eval(final String cmd) throws HaskellException;
    public abstract void close() throws HaskellException;
}
