package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.type.ConstT;
import org.junit.Assert;
import org.junit.Test;

/** Tests for the Haskell catalog. */
public class HaskellClassCatalogTest {
    @Test
    public void basicCatalogTest() throws Exception {
        HaskellClassCatalog hc = new HaskellClassCatalog();

        // Test our sanity.
        Assert.assertNotNull(hc);

        // getEntry
        ClassEntry e = hc.getEntry("Num");
        Assert.assertNotNull(e);
        Assert.assertEquals("Num", e.getName());
        Assert.assertEquals(3, e.getInstances().size());
        Assert.assertTrue(e.getInstances().contains("Int"));
        Assert.assertTrue(e.getInstances().contains("Float"));
        Assert.assertTrue(e.getInstances().contains("Double"));

        // getMaybe
        Assert.assertTrue(hc.getMaybe("Num").isPresent());
        Assert.assertFalse(hc.getMaybe("\0").isPresent());

        // getOrDefault
        Assert.assertNotNull(hc.getOrDefault("Num", null));
        Assert.assertNull(hc.getOrDefault("\0", null));
    }
}
