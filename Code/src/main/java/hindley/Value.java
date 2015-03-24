package hindley;

class Value extends Expr {
    private String val;
    private Type type;

    public Value(Type type, String val) {
        this.type = type;
        this.val = val;
    }

    @Override
    public Type analyze(Env env, GenSet nonGen) {
        return type;
    }

    public String toString() {
        return val;
    }
}
