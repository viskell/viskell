package nl.utwente.group10.haskell.typeparser;

import org.junit.Assert;
import org.junit.Test;

/** Tests the TypeBuilder */
public class TypeBuilderTest {
    /**
     * Test helper that checks if parsing a String and then converting it back
     * gives the same result.
     */
    private void roundtrip(String hs) {
        Assert.assertEquals(hs, new TypeBuilder().build(hs).toHaskellType());
    }

    @Test public void testBuildConstT() { this.roundtrip("Int"); }
    @Test public void testBuildVarT()   { this.roundtrip("a"); }
    @Test public void testBuildFuncT()  { this.roundtrip("(a -> b)"); }
    @Test public void testBuildListT()  { this.roundtrip("[a]"); }
    @Test public void testBuildTupleT() { this.roundtrip("(String, Int)"); }
    @Test public void testKitchenSink() { this.roundtrip("([a] -> ([b] -> [(a, b)]))"); }
}
