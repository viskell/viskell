package nl.utwente.viskell.ui;

import com.google.common.collect.ImmutableMap;
import com.sun.javafx.collections.ImmutableObservableList;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ui.components.Block;
import nl.utwente.viskell.ui.components.SimulateBlock;
import nl.utwente.viskell.ui.serialize.Bundleable;
import org.junit.Before;
import org.junit.Ignore;
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
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Unit tests for Serialization/Deserialization from ToplevelPane
 * Created by andrew on 27/03/16.
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

    /*
    Couldn't get this test to work. To try it you should change the line at the start of the test class to be:
    @PrepareForTest({ToplevelPane.class, Pane.class, Block.class})

    I need to @PrepareForTest ToplevelPane.class to get whenNew() to work -
    but it seems to fail due to a problem related to javassist and java8...

    Error currently reported is:
        "java.lang.IllegalStateException: Failed to transform class with name nl.utwente.viskell.ui.ToplevelPane.
         Reason: javassist.bytecode"


    Spent about a day in junit/powermock/javassist dependency hell trying many different versions
    of junit and powermock that produce different failures I couldn't solve either....such as
            "java.lang.VerifyError: Inconsistent stackmap frames at branch target 1135"
            (that one seems to be Java 7 related and can be avoided passing "-noverify" to JVM when running tests,
            but that seems a hack)

    ....then I gave up...
     */
    @Ignore("Fails due to powermock problems")
    @Test
    public void addsBlockFromBundleFromBundleTest() throws Exception {
        // Setup
        Pane mockLayer = mock(Pane.class, RETURNS_DEEP_STUBS);
        when(mockLayer.getChildren().add(any())).thenReturn(true);
        whenNew(Pane.class).withAnyArguments().thenReturn(mockLayer);

        mockStatic(Block.class);
        SimulateBlock mockBlock = mock(SimulateBlock.class);
        when(Block.fromBundle(any(), any(), any())).thenReturn(mockBlock);

        List<Map<String, Object>> arrayOfBlockBundles = new ArrayList<>();
        Map<String, Object> blockBundle = ImmutableMap.of(Bundleable.KIND, SimulateBlock.class.getSimpleName());
        arrayOfBlockBundles.add(0, blockBundle);

        Map<String, Object> savedBundle = new HashMap<>();
        savedBundle.put(ToplevelPane.BLOCKS_SERIALIZED_NAME, arrayOfBlockBundles);
        // No Connections

        // UUT
        ToplevelPane toplevelPane = new ToplevelPane(mockGhci);

        // Test
        toplevelPane.fromBundle(savedBundle);

        // Verify
        verify(toplevelPane, times(1)).addBlock(mockBlock);
    }
}