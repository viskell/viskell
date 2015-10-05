package nl.utwente.group10.haskell.typeparser;

import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeClass;
import org.junit.Assert;
import org.junit.Test;


/** Tests the TypeBuilder */
public class TypeBuilderTest {
    /**
     * Test helper that checks if parsing a String and then converting it back
     * gives the same result.
     */
    private void roundtrip(String hs) {
        convert(hs, hs);
    }

    /**
     * Test helper that checks if parsing a String gives the expected result.
     */
    private void convert(String from, String to) {
        Assert.assertEquals(to, new Environment().buildType(from).toHaskellType());
    }

    @Test public void testBuildConstT() { this.roundtrip("Int"); }
    @Test public void testBuildVarT()   { this.roundtrip("a"); }
    @Test public void testBuildFuncT()  { this.roundtrip("a -> b"); }
    @Test public void testBuildListT()  { this.roundtrip("[a]"); }
    @Test public void testBuildTupleT() { this.roundtrip("(String, Int)"); }
    @Test public void testMaybeA()      { this.roundtrip("Maybe a"); }
    @Test public void testMaybeInt()    { this.roundtrip("Maybe Int"); }
    @Test public void testMaybeFunc()   { this.roundtrip("Maybe (Int -> Int)"); }
    @Test public void testMaybeMaybe()  { this.roundtrip("Maybe (Maybe a)"); }
    @Test public void testMaybeSink()   { this.roundtrip("Maybe Int -> Maybe [a]"); }
    @Test public void testKitchenSink() { this.roundtrip("[a] -> [b] -> [(a, b)]"); }

    @Test public void testPrefixUnit()  { this.convert("()", "()"); }
    @Test public void testPrefixTuple() { this.convert("(,) a b", "(a, b)"); }
    @Test public void testPrefixTriple(){ this.convert("(,,) a b c", "(a, b, c)"); }
    @Test public void testPrefixList()  { this.convert("[] a", "[a]"); }

    @Test public void testTypeClass()   {
        Environment env = new Environment();
        env.addTypeClass(new TypeClass("Num", Type.con("Int"), Type.con("Float"), Type.con("Double")));
        env.addTypeClass(new TypeClass("Eq", Type.con("Int"), Type.con("Float"), Type.con("Double"), Type.con("Char"), Type.con("Bool")));
        env.addTypeClass(new TypeClass("Functor"));

        Assert.assertEquals("(Num a)", env.buildType("Num a => a").toHaskellType());
        Assert.assertEquals("(Num a) -> (Num a)", env.buildType("(Num a) => (a -> a)").toHaskellType());
        Assert.assertEquals("(Num a) -> b", env.buildType("(Num a, Nonexistent b) => a -> b").toHaskellType());
        Assert.assertEquals("(Num a) -> (Eq b)", env.buildType("(Num a, Eq b) => a -> b").toHaskellType());
        Assert.assertEquals("(a -> b) -> (Functor f) a -> (Functor f) b", env.buildType("Functor f => (a -> b) -> f a -> f b").toHaskellType());
    }
}
