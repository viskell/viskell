package nl.utwente.group10.haskell.catalog;

import org.junit.Assert;
import org.junit.Test;

/** Tests for the Haskell catalog. */
public class HaskellCatalogTest {
    @Test
    public void basicCatalogTest() throws Exception {
        HaskellCatalog hc = new HaskellCatalog();

        // Test our sanity.
        Assert.assertNotNull(hc);

        // getEntry
        Entry e = hc.getEntry("id");
        Assert.assertNotNull(e);
        Assert.assertEquals("id", e.getName());
        Assert.assertEquals("a -> a", e.getSignature());
        Assert.assertEquals("Identity function.", e.getDocumentation());
        Assert.assertFalse(e.getCategory().isEmpty());

        // asEnvFunc
        Assert.assertEquals("id", e.asEnvFunc().toHaskell());
        Assert.assertNotNull(e.asEnvFunc().getType());

        // getMaybe
        Assert.assertTrue(hc.getMaybe("id").isPresent());
        Assert.assertFalse(hc.getMaybe("\0").isPresent());

        // getOrDefault
        Assert.assertNotNull(hc.getOrDefault("id", null));
        Assert.assertNull(hc.getOrDefault("\0", null));

        // getCategories
        Assert.assertNotNull(hc.getCategories());
        Assert.assertFalse(hc.getCategories().isEmpty());
        Assert.assertTrue(hc.getCategories().contains("Math"));

        // getCategory
        Assert.assertNotNull(hc.getCategory("Math"));
        Assert.assertFalse(hc.getCategory("Math").isEmpty());
        Assert.assertFalse(hc.getCategory(e.getCategory()).isEmpty());
    }
}
