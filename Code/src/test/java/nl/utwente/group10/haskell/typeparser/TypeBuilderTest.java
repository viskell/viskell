package nl.utwente.group10.haskell.typeparser;

import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeClass;
import nl.utwente.group10.haskell.type.VarT;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
    @Test public void testMaybeA()      { this.roundtrip("Maybe a"); }
    @Test public void testMaybeInt()    { this.roundtrip("Maybe Int"); }
    @Test public void testMaybeFunc()   { this.roundtrip("Maybe (Int -> Int)"); }
    @Test public void testMaybeSink()   { this.roundtrip("(Maybe Int -> Maybe [a])"); }
    @Test public void testKitchenSink() { this.roundtrip("([a] -> ([b] -> [(a, b)]))"); }

    @Test public void testTypeClass()   {
        Map<String, TypeClass> typeClasses = new HashMap<>();
        typeClasses.put("Num", new TypeClass("Num", new ConstT("Int"), new ConstT("Float"), new ConstT("Double")));
        TypeBuilder builder = new TypeBuilder(typeClasses);

        Assert.assertEquals("((Num a) -> (Num a))", builder.build("(Num a) => (a -> a)").toHaskellType());
    }
}
