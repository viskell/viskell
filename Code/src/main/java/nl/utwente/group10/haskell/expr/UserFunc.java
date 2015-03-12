package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.FuncT;

public class UserFunc extends Func {
    // TODO: arguments

    private final String name;
    private final Expr body;

    public UserFunc(String name, Expr body) {
        super(new FuncT(body.getType()));
        this.name = name;
        this.body = body;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toHaskell() {
        return String.format("let %s = %s", this.name, this.body.toHaskell());
    }

    @Override
    public String toString() {
        return "UserFunc{" +
                "name='" + this.name + '\'' +
                ", body=" + this.body +
                '}';
    }
}
