package nl.utwente.group10.haskell.typeparser;

import nl.utwente.group10.haskell.type.Type;
import org.junit.Test;

public class TypeBuilderTest {
    @Test
    public void testBuildVarT() throws Exception {
        new TypeBuilder().build("a");
    }

    @Test
    public void testBuildKitchensink() throws Exception {
        Type result = new TypeBuilder().build("[a] -> [b] -> [(a, b)]");
        System.out.println(result.toString());
    }
}
