package hindley;

import nl.utwente.group10.ghcj.GhciEvaluator;
import nl.utwente.group10.ghcj.GhciException;

import java.io.IOException;

/** Test class for type inference. */
public class InferencePlayground {
    public static void main(String[] params) throws IOException, GhciException {
        Type boolType = new TypeOp("Bool");
        Type intType = new TypeOp("Int");
        Type tupleOfInts = new TupleT(intType, intType);
        Type listOfBools = new ListT(boolType);

        Type alpha = HindleyMilner.makeTypeVar();
        Type beta = HindleyMilner.makeTypeVar();
        Type gamma = HindleyMilner.makeTypeVar();
        Type delta = HindleyMilner.makeTypeVar();

        Env env = new Env();
        env.put("id", new FuncT(alpha, alpha));
        env.put("seven", new TypeOp("Int"));
        env.put("length", new FuncT(new TypeOp("List", beta), intType));
        env.put("(+)", new FuncT(intType, new FuncT(intType, intType)));
        env.put("const", new FuncT(alpha, new FuncT(gamma, delta)));

        System.out.println("Environment:");
        System.out.println(env);
        System.out.println();

        Expr program =
            new Apply(
                new Apply(
                    new Ident("(+)"),
                    new Value(intType, "10")
                ),
                new Apply(
                    new Ident("length"),
                    new Apply(
                        new Apply(
                            new Ident("const"),
                            new Value(listOfBools, "[]")
                        ),
                        new Value(tupleOfInts, "(1, 2)")
                    )
                )
            );

        System.out.println("Expression:");
        System.out.println(program);
        System.out.println();

        Type result = program.analyze(env, new GenSet());

        System.out.println("We inferred:");
        System.out.println(result);
        System.out.println();

        System.out.println("GHCi said:");
        GhciEvaluator eval = new GhciEvaluator();
        System.out.println(eval.eval(String.format(":type %s", program)));
        eval.close();
    }

}

