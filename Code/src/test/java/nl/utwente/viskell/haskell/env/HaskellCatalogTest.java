package nl.utwente.viskell.haskell.env;

import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import org.junit.Test;

import static org.junit.Assert.*;

public class HaskellCatalogTest {
    @Test
    public void basicCatalogTest() {
        HaskellCatalog c = new HaskellCatalog();

        // getCategories
        assertFalse(c.getCategories().isEmpty());
        assertTrue(c.getCategories().contains("List operations"));

        // asEnvironment
        Environment e = c.asEnvironment();
        assertNotNull(e.lookupFun("id"));
        assertNotNull(e.testLookupClass("Num"));
        assertNotNull(e.lookupFun("(+)"));

        // getByPredicate
        assertEquals(c.size(), c.getByPredicate(fn -> true).size());
        assertEquals(0, c.getByPredicate(fn -> false).size());

        // getByPrefix
        assertEquals(c.size(), c.getByPrefix("").size());
        assertEquals(0, c.getByPrefix("nosuchfunction").size());
        assertEquals(1, c.getByPrefix("(+)").size());

        // getByType
        assertEquals(c.size(), c.getByType(new TypeScope().getVar("a")).size());
        assertEquals(1, c.getByType(Type.con("Banana")).size());
    }
}
