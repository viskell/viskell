package nl.utwente.group10.ghcj;

import org.junit.Assert;
import org.junit.Test;

public class GhciEnvironmentTest {
    @Test
    public void putStrLnTest() throws Exception {
        GhciSession session = new GhciSession();
        GhciEnvironment env = session.getEnvironment();
        Assert.assertTrue(!env.getBindings().isEmpty());
        session.close();
    }
}
