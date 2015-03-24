package hindley;

class Apply extends Expr {
    Expr fn;
    Expr arg;

    public Apply(Expr fn, Expr arg) {
        this.fn = fn;
        this.arg = arg;
    }

    public String toString() {
        return String.format("(%s %s)", fn, arg);
    }

    @Override
    public Type analyze(Env env, GenSet nonGen) {
        Type funtype = fn.analyze(env, nonGen);
        Type argtype = arg.analyze(env, nonGen);
        Type restype = HindleyMilner.makeTypeVar();

        HindleyMilner.unify(new FuncT(argtype, restype), funtype);

        return restype;
    }
}
