package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.env.HaskellCatalog;
import nl.utwente.group10.haskell.exceptions.CatalogException;

import static org.junit.Assert.*;

import org.junit.Test;

public class HaskellCatalogTest {
    @Test
    public void basicCatalogTest() throws CatalogException {
        HaskellCatalog c = new HaskellCatalog();

        // getCategories
        assertFalse(c.getCategories().isEmpty());
        assertTrue(c.getCategories().contains("Basic"));

        // asEnvironment
        Environment e = c.asEnvironment();
        assertTrue(e.getFreshExprType("id").isPresent());
        assertNotNull(e.lookupClass("Num"));
        assertTrue(e.getFreshExprType("(+)").isPresent());
    }
}
