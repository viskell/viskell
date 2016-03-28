package nl.utwente.viskell.ui;

import com.google.common.base.Charsets;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import nl.utwente.viskell.ui.serialize.Exporter;
import nl.utwente.viskell.ui.serialize.Importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * menu actions
 */
public class MenuActions {
    /**
     * The main overlay of which this menu is a part
     */
    protected MainOverlay overlay;

    /**
     * The File we're currently working on, if any.
     */
    private Optional<File> currentFile;

    /** The current preferences window, or null if not yet opened. */
    private PreferencesWindow preferences;

    /** The current inspector window, or null if not yet opened. */
    private InspectorWindow inspector;

    public MenuActions(MainOverlay ol) {
        overlay = ol;
        currentFile = Optional.empty();
    }

    protected List<MenuItem> fileMenuItems() {
        List<MenuItem> list = new ArrayList<>();

        MenuItem menuNew = new MenuItem("New");
        menuNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        menuNew.setOnAction(this::onNew);
        list.add(menuNew);

        MenuItem menuOpen = new MenuItem("Open...");
        menuOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        menuOpen.setOnAction(this::onOpen);
        list.add(menuOpen);

        MenuItem menuSave = new MenuItem("Save");
        menuSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        menuSave.setOnAction(this::onSave);
        list.add(menuSave);

        MenuItem menuSaveAs = new MenuItem("Save as...");
        menuSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN));
        menuSaveAs.setOnAction(this::onSaveAs);
        list.add(menuSaveAs);

        return list;
    }

    @SuppressWarnings("UnusedParameters")
    protected void showPreferences(ActionEvent actionEvent) {
        if (preferences == null) {
            preferences = new PreferencesWindow(overlay);
        }

        preferences.show();
    }

    @SuppressWarnings("UnusedParameters")
    protected void showInspector(ActionEvent actionEvent) {
        if (inspector == null) {
            inspector = new InspectorWindow(overlay);
        }

        inspector.show();
    }

    protected void onNew(ActionEvent actionEvent) {
        overlay.getToplevelPane().clearChildren();
    }

    protected void onOpen(ActionEvent actionEvent) {
        Window window = overlay.getScene().getWindow();
        File file = new FileChooser().showOpenDialog(window);

        if (file != null) {
            addChildrenFrom(file, overlay.getToplevelPane());
        }
    }

    protected void onSave(ActionEvent actionEvent) {
        if (currentFile.isPresent()) {
            saveTo(currentFile.get());
        } else {
            onSaveAs(actionEvent);
        }
    }

    protected void onSaveAs(ActionEvent actionEvent) {
        Window window = overlay.getScene().getWindow();
        File file = new FileChooser().showSaveDialog(window);

        if (file != null) {
            saveTo(file);
            currentFile = Optional.of(file);
        }
    }

    protected void addChildrenFrom(File file, ToplevelPane toplevelPane) {
        try (FileInputStream fis = new FileInputStream(file)) {
            Map<String, Object> layers = Importer.readLayers(fis);
            toplevelPane.fromBundle(layers);
            fis.close();
        } catch (IOException e) {
            // TODO do something sensible here - like show a dialog
            e.printStackTrace();
        }
    }

    protected void saveTo(File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(Exporter.export(overlay.getToplevelPane()).getBytes(Charsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            // TODO do something sensible here
            e.printStackTrace();
        }
    }

    @SuppressWarnings("UnusedParameters")
    protected void toggleFullScreen(ActionEvent actionEvent) {
        Stage stage = Main.primaryStage;
        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
        } else {
            stage.setMaximized(true);
            stage.setFullScreen(true);
        }
    }

    @SuppressWarnings("UnusedParameters")
    protected void onQuit(ActionEvent actionEvent) {
        Platform.exit();
    }

    @SuppressWarnings("UnusedParameters")
    protected void zoomIn(ActionEvent actionEvent) {
        overlay.getToplevelPane().zoom(1.1);
    }

    @SuppressWarnings("UnusedParameters")
    protected void zoomOut(ActionEvent actionEvent) {
        overlay.getToplevelPane().zoom(1 / 1.1);
    }
}
