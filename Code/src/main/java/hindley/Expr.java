package hindley;

abstract class Expr {
    public abstract Type analyze(Env env, GenSet nonGen);
}
