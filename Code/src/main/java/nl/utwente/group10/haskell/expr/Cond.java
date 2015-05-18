package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;

public class Cond extends Expr {
    private Expr condition;
    private Expr trueCase;
    private Expr falseCase;

    public final String TPL = "(if %s then %s else %s)";

    public Cond(Expr condition, Expr trueCase, Expr falseCase) {
        this.condition = condition;
        this.trueCase = trueCase;
        this.falseCase = falseCase;
    }

    @Override
    public Type analyze(Env env, GenSet genSet) throws HaskellException {
        final Type condType = condition.analyze(env, genSet);
        final Type trueType = trueCase.analyze(env, genSet);
        final Type falseType = falseCase.analyze(env, genSet);
        final Type resType = HindleyMilner.makeVariable();

        // Condition must be a boolean
        HindleyMilner.unify(this, condType, new ConstT("Bool"));

        // Branches must have compatible types
        HindleyMilner.unify(this, trueType, falseType);

        // Figure out the result type
        HindleyMilner.unify(this, trueType, resType);

        return resType;
    }

    @Override
    public String toHaskell() {
        return String.format(TPL, condition.toHaskell(), trueCase.toHaskell(), falseCase.toHaskell());
    }

    @Override
    public String toString() {
        return String.format(TPL, condition, trueCase, falseCase);
    }
}
