package nl.utwente.viskell.ui;

import com.google.common.base.Charsets;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import nl.utwente.viskell.ui.serialize.Exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * A context menu with global actions (i.e. quit).
 */
public class GlobalMenu extends ContextMenu {
    /** The main overlay of which this menu is a part */ 
    private MainOverlay overlay;

    /** The File we're currently working on, if any. */
    private Optional<File> currentFile;


    public GlobalMenu(MainOverlay overlay) {
        super();
        this.overlay = overlay;
        this.currentFile = Optional.empty();

        MenuItem menuNew = new MenuItem("New");
        menuNew.setOnAction(this::onNew);

        MenuItem menuOpen = new MenuItem("Open...");
        menuOpen.setOnAction(this::onOpen);

        MenuItem menuSave = new MenuItem("Save");
        menuSave.setOnAction(this::onSave);

        MenuItem menuSaveAs = new MenuItem("Save as...");
        menuSaveAs.setOnAction(this::onSaveAs);

        MenuItem menuPreferences = new MenuItem("Preferences...");
        menuPreferences.setOnAction(e -> this.overlay.showPreferences());

        MenuItem menuInspector = new MenuItem("Inspector");
        menuInspector.setOnAction(e -> this.overlay.showInspector());

        MenuItem menuQuit = new MenuItem("Quit");
        menuQuit.setOnAction(this::onQuit);

        this.getItems().addAll(menuNew, menuOpen, menuSave, menuSaveAs, menuInspector, menuPreferences, menuQuit);
    }

    private void onNew(ActionEvent actionEvent) {
        this.overlay.getMainPane().clearChildren();
    }

    private void onOpen(ActionEvent actionEvent) {
        Window window = this.overlay.getScene().getWindow();
        File file = new FileChooser().showOpenDialog(window);

        if (file != null) {
            /* Load file... */
        }
    }

    private void onSave(ActionEvent actionEvent) {
        if (this.currentFile.isPresent()) {
            saveTo(this.currentFile.get());
        } else {
            onSaveAs(actionEvent);
        }
    }

    private void onSaveAs(ActionEvent actionEvent) {
        Window window = this.overlay.getScene().getWindow();
        File file = new FileChooser().showSaveDialog(window);

        if (file != null) {
            saveTo(file);
            this.currentFile = Optional.of(file);
        }
    }

    private void saveTo(File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(Exporter.export(this.overlay.getMainPane()).getBytes(Charsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            // TODO do something sensible here
            e.printStackTrace();
        }
    }

    private void onQuit(ActionEvent actionEvent) {
        Platform.exit();
    }
}
