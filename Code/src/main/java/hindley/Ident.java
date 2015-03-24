package hindley;

class Ident extends Expr {
    private String name;

    public Ident(String name) {
        this.name = name;
    }

    @Override
    public Type analyze(Env env, GenSet nonGen) {
        return env.get(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
