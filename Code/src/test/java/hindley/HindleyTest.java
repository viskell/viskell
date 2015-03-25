package hindley;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HindleyTest {
    private final Type alpha = new TypeVar("a");
    private final Type alpha2 = new TypeVar("a");
    private final Type beta = new TypeVar("b");
    private final Type gamma = new TypeVar("c");
    private final Type alphaList = new ListT(this.alpha);
    private final Type betaList = new ListT(this.beta);
    private final Type integer = new TypeOp("Int");
    private final Type integerList = new ListT(this.integer);
    private final Type string = new TypeOp("Str");
    private final Type stringList = new ListT(this.string);

    Env env;

    @Before
    public void setUp() {
        this.env = new Env();

        final FuncT mapT = new FuncT(new FuncT(this.alpha, this.beta), new FuncT(this.alphaList, this.betaList));
        final FuncT zipT = new FuncT(this.alphaList, new FuncT(this.betaList, new ListT(new TupleT(this.alpha, this.beta))));
        final FuncT lcmT = new FuncT(this.alpha, new FuncT(this.alpha, this.alpha));
        final FuncT idT = new FuncT(this.gamma, this.gamma);

        this.env.put("map", mapT);
        this.env.put("zip", zipT);
        this.env.put("lcm", lcmT);
        this.env.put("id", idT);
    }

    @Test
    public void testSum() {
        final FuncT sumT1 = new FuncT(this.alpha, this.alpha);
        final FuncT sumT2 = new FuncT(this.alpha, this.alpha2);

        this.env.put("sum1", sumT1);
        this.env.put("sum2", sumT2);

        final Apply apply1 = new Apply(new Ident("sum1"), new Value(this.integer, "TbD"));
        final Apply apply2 = new Apply(new Ident("sum2"), new Value(this.integer, "TbD"));

        assertEquals(this.integer, apply1.analyze(this.env, new GenSet()).prune());
        assertEquals(new TypeVar("β").toString(), ((TypeVar) apply2.analyze(this.env, new GenSet())).toString());
    }

    @Test
    public void testMapId() {
        final Apply apply1 = new Apply(new Ident("map"), new Ident("id"));
        final Apply apply2 = new Apply(apply1, new Value(this.integerList, "[1, 2, 3]"));

        assertEquals(this.integerList.toString(), apply2.analyze(this.env, new GenSet()).prune().toString());
    }

    @Test
    public void testZip() {
        final Apply apply1 = new Apply(new Ident("zip"), new Value(this.stringList, "[\"A\", \"B\"]"));
        final Apply apply2 = new Apply(apply1, new Value(this.integerList, "[1, 2, 3]"));

        assertEquals(new ListT(new TupleT(this.string, this.integer)).toString(), apply2.analyze(this.env, new GenSet()).prune().toString());
    }

    @Test(expected=RuntimeException.class)
    public void testInvalidLcm() {
        final Apply apply1 = new Apply(new Ident("lcm"), new Value(this.integer, "123"));
        final Apply brokenApply = new Apply(apply1, new Value(this.string, "\"ABC\""));

        assertNotEquals(this.integer.toString(), brokenApply.analyze(this.env, new GenSet()).prune().toString());
        assertNotEquals(this.string.toString(), brokenApply.analyze(this.env, new GenSet()).prune().toString());
    }
}
