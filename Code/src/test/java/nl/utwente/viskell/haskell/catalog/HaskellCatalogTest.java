package nl.utwente.viskell.haskell.catalog;

import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
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
    }
}
