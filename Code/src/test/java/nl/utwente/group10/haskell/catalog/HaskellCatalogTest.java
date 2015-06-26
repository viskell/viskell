package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HaskellCatalogTest {
    @Test
    public void basicCatalogTest() throws CatalogException {
        HaskellCatalog c = new HaskellCatalog();

        // getCategories
        assertFalse(c.getCategories().isEmpty());
        assertTrue(c.getCategories().contains("Basic"));

        // asEnvironment
        Env e = c.asEnvironment();
        assertFalse(e.getExprTypes().isEmpty());
        assertFalse(e.getTypeClasses().isEmpty());
        assertTrue(e.getExprTypes().containsKey("id"));
        assertTrue(e.getTypeClasses().containsKey("Num"));
        assertTrue(e.getFreshExprType("(+)").isPresent());
    }
}
