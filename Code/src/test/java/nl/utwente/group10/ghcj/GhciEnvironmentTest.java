package nl.utwente.group10.ghcj;

import org.junit.Test;
import static org.junit.Assert.*;

public class GhciEnvironmentTest {
    @Test
    public void putStrLnTest() throws GhciException {
        GhciEnvironment env = new GhciSession().getEnvironment();
        env.getBindings();
    }
}
