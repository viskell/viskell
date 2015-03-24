package hindley;

class Ident extends Expr {
    private String name;

    public Ident(String name) {
        this.name = name;
    }

    @Override
    public Type analyze(Env env, GenSet nonGen) {
        // Rule [Var]:
        // IFF  we know (from the env) that the type of this expr is x
        // THEN the type of this expr is x.
        return env.get(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
