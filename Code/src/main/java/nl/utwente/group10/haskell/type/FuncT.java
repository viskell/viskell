package nl.utwente.group10.haskell.type;

import com.google.common.base.Joiner;

public class FuncT extends Type {
    private Type[] arguments;

    public FuncT(Type... arguments) {
        this.arguments = arguments;
    }

    public String toString() {
        return "(" + Joiner.on(" -> ").join(arguments) + ")";
    }
}
