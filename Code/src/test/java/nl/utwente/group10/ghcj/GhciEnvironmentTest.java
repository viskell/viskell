package nl.utwente.group10.ghcj;

import org.junit.Assert;
import org.junit.Test;

public class GhciEnvironmentTest {
    @Test
    public void putStrLnTest() throws GhciException {
        GhciEnvironment env = new GhciSession().getEnvironment();
        Assert.assertTrue(!env.getBindings().isEmpty());
    }
}
