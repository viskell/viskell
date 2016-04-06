package nl.utwente.viskell.ui;

import com.google.common.collect.ImmutableMap;
import com.sun.javafx.collections.ImmutableObservableList;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ui.components.Block;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Unit tests for Serialization/Deserialization from ToplevelPane
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Pane.class, Block.class})
public class ToplevelPaneTest {
    private GhciSession mockGhci;

    @Before
    public void Setup() {
        mockGhci = mock(GhciSession.class);
    }

    @Test
    public void emptyLayersNoBlocksToBundleTest() throws Exception {
        // Setup
        Pane mockLayer = mock(Pane.class);
        when(mockLayer.getChildren()).thenReturn(new ImmutableObservableList<>());
        whenNew(Pane.class).withArguments(any()).thenReturn(mockLayer);

        // UUT
        ToplevelPane toplevelPane = new ToplevelPane(mockGhci);

        // Test
        Map<String, Object> bundle = toplevelPane.toBundle();

        // Verify
        Object[] blocks = (Object[])bundle.get(ToplevelPane.BLOCKS_SERIALIZED_NAME);
        assertEquals(blocks.length, 0);
    }

    @Test
    public void emptyLayersNoConnectionsToBundleTest() throws Exception {
        // Setup
        Pane mockLayer = mock(Pane.class);
        when(mockLayer.getChildren()).thenReturn(new ImmutableObservableList<>());
        whenNew(Pane.class).withArguments(any()).thenReturn(mockLayer);

        // UUT
        ToplevelPane toplevelPane = new ToplevelPane(mockGhci);

        // Test
        Map<String, Object> bundle = toplevelPane.toBundle();

        // Verify
        Object[] connections = (Object[]) bundle.get(ToplevelPane.CONNECTIONS_SERIALIZED_NAME);
        assertEquals(connections.length, 0);
    }

    @Test
    public void nullBundleFromBundleTest() {
        // UUT
        ToplevelPane toplevelPane = new ToplevelPane(mockGhci);

        // Test
        toplevelPane.fromBundle(null); // no crash
    }

    @Test
    public void emptyBundleFromBundleTest() {
        // UUT
        ToplevelPane toplevelPane = new ToplevelPane(mockGhci);

        // Test
        toplevelPane.fromBundle(ImmutableMap.of()); // no crash
    }

    @Test
    public void nullBlocksBundleFromBundleTest() {
        // Setup
        Map<String, Object> blocksBundle = new HashMap<>();
        blocksBundle.put(ToplevelPane.BLOCKS_SERIALIZED_NAME, null);
        // no connections bundle

        // UUT
        ToplevelPane toplevelPane = new ToplevelPane(mockGhci);

        // Test
        toplevelPane.fromBundle(blocksBundle); // no crash
    }

    @Test
    public void nullConnectionsBundleFromBundleTest() {
        // Setup
        Map<String, Object> connectionsBundle = new HashMap<>();
        connectionsBundle.put(ToplevelPane.CONNECTIONS_SERIALIZED_NAME, null);
        // no blocks bundle

        // UUT
        ToplevelPane toplevelPane = new ToplevelPane(mockGhci);

        // Test
        toplevelPane.fromBundle(connectionsBundle); // no crash
    }

    @Test
    public void emptyBlocksListFromBundleTest() {
        // Setup
        Map<String, Object> blocksBundle = new HashMap<>();
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<Map<String, Object>> blocks = new ArrayList<>();
        blocksBundle.put(ToplevelPane.BLOCKS_SERIALIZED_NAME, blocks);

        // UUT
        ToplevelPane toplevelPane = new ToplevelPane(mockGhci);

        // Test
        toplevelPane.fromBundle(blocksBundle); // no crash
    }

    @Test
    public void emptyConnectionsListFromBundleTest() {
        // Setup
        Map<String, Object> connectionsBundle = new HashMap<>();
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<Map<String, Object>> connections = new ArrayList<>();
        connectionsBundle.put(ToplevelPane.CONNECTIONS_SERIALIZED_NAME, connections);

        // UUT
        ToplevelPane toplevelPane = new ToplevelPane(mockGhci);

        // Test
        toplevelPane.fromBundle(connectionsBundle); // no crash
    }
}