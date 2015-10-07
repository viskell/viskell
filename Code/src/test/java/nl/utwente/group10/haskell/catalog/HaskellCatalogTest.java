package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.env.HaskellCatalog;

import static org.junit.Assert.*;

import org.junit.Test;

public class HaskellCatalogTest {
    @Test
    public void basicCatalogTest() {
        HaskellCatalog c = new HaskellCatalog();

        // getCategories
        assertFalse(c.getCategories().isEmpty());
        assertTrue(c.getCategories().contains("Basic"));

        // asEnvironment
        Environment e = c.asEnvironment();
        assertNotNull(e.lookupFun("id"));
        assertNotNull(e.testLookupClass("Num"));
        assertNotNull(e.lookupFun("(+)"));
    }
}
