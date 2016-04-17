package nl.utwente.viskell.ui;

import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Unit tests for MenuActions
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MenuActions.class, Main.class, Stage.class, MainOverlay.class, PreferencesWindow.class, InspectorWindow.class})
public class MenuActionsTest {
    private MainOverlay mockMainOverlay;
    private ActionEvent mockEvent;


    @Before
    public void Setup() throws Exception {
        mockMainOverlay = mock(MainOverlay.class);
        Stage mockStage = mock(Stage.class);
        mockStatic(Main.class);
        when(Main.getStage()).thenReturn(mockStage);
        mockEvent = mock(ActionEvent.class);
    }

    @Test
    public void initialisesWithANewFileTest() {
        // Mocks
        ToplevelPane mockToplevelPane = mock(ToplevelPane.class);

        // UUT
        MenuActions menuActions = new MenuActions(mockMainOverlay, mockToplevelPane);

        // Access
        Optional<File> currentFile = Whitebox.getInternalState(menuActions, "currentFile");

        // verify
        assertThat(currentFile.isPresent(), is(false));
    }

    @Test
    public void showPreferencesTest() throws Exception {
        // Mocks
        ToplevelPane mockToplevelPane = mock(ToplevelPane.class);
        PreferencesWindow mockPreferencesWindow = mock(PreferencesWindow.class);
        whenNew(PreferencesWindow.class).withArguments(any()).thenReturn(mockPreferencesWindow);

        // UUT
        MenuActions menuActions = new MenuActions(mockMainOverlay, mockToplevelPane);

        // Test
        menuActions.showPreferences(mockEvent);

        // verify
        verify(mockPreferencesWindow, times(1)).show();
    }

    @Test
    public void showInspectorTest() throws Exception {
        // Mocks
        ToplevelPane mockToplevelPane = mock(ToplevelPane.class);
        InspectorWindow mockInspectorWindow = mock(InspectorWindow.class);
        whenNew(InspectorWindow.class).withArguments(any()).thenReturn(mockInspectorWindow);

        // UUT
        MenuActions menuActions = new MenuActions(mockMainOverlay, mockToplevelPane);

        // Test
        menuActions.showInspector(mockEvent);

        // verify
        verify(mockInspectorWindow, times(1)).show();
    }

    @Test
    public void zoomInTest() {
        // mocks
        ToplevelPane mockToplevelPane = mock(ToplevelPane.class);
        when(mockMainOverlay.getToplevelPane()).thenReturn(mockToplevelPane);

        // UUT
        MenuActions menuActions = new MenuActions(mockMainOverlay, mockToplevelPane);

        // Test
        menuActions.zoomIn(mockEvent);

        // verify
        verify(mockToplevelPane, times(1)).zoom(1.1);
    }

    @Test
    public void zoomOutTest() {
        // mocks
        ToplevelPane mockToplevelPane = mock(ToplevelPane.class);
        when(mockMainOverlay.getToplevelPane()).thenReturn(mockToplevelPane);

        // UUT
        MenuActions menuActions = new MenuActions(mockMainOverlay, mockToplevelPane);

        // Test
        menuActions.zoomOut(mockEvent);

        // verify
        verify(mockToplevelPane, times(1)).zoom(1/1.1);
    }
}
