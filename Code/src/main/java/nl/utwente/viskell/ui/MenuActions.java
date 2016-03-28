package nl.utwente.viskell.ui;

import com.google.common.base.Charsets;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import nl.utwente.viskell.ui.serialize.Exporter;
import nl.utwente.viskell.ui.serialize.Importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * menu actions
 */
public class MenuActions {
    /** The main overlay of which this menu is a part */ 
    protected MainOverlay overlay;

    /** The File we're currently working on, if any. */
    private Optional<File> currentFile;

    public MenuActions(MainOverlay overlay) {
        this.overlay = overlay;
        this.currentFile = Optional.empty();
    }

    protected List<MenuItem> fileMenuItems() {
        List<MenuItem> list = new ArrayList<>();

        MenuItem menuNew = new MenuItem("New");
        menuNew.setOnAction(this::onNew);
        list.add(menuNew);

        MenuItem menuOpen = new MenuItem("Open...");
        menuOpen.setOnAction(this::onOpen);
        list.add(menuOpen);

        MenuItem menuSave = new MenuItem("Save");
        menuSave.setOnAction(this::onSave);
        list.add(menuSave);

        MenuItem menuSaveAs = new MenuItem("Save as...");
        menuSaveAs.setOnAction(this::onSaveAs);
        list.add(menuSaveAs);

        return list;
    }

    @SuppressWarnings("UnusedParameters")
    protected void showPreferences(ActionEvent actionEvent) {
        this.overlay.showPreferences();
    }

    @SuppressWarnings("UnusedParameters")
    protected void showInspector(ActionEvent actionEvent) {
        this.overlay.showInspector();
    }

    protected void onNew(ActionEvent actionEvent) {
        this.overlay.getToplevelPane().clearChildren();
        this.currentFile = Optional.empty();
    }

    protected void onOpen(ActionEvent actionEvent) {
        Window window = this.overlay.getScene().getWindow();
        File file = new FileChooser().showOpenDialog(window);

        if (file != null) {
            addChildrenFrom(file, this.overlay.getToplevelPane());
        }
    }

    protected void onSave(ActionEvent actionEvent) {
        if (this.currentFile.isPresent()) {
            saveTo(this.currentFile.get());
        } else {
            onSaveAs(actionEvent);
        }
    }

    protected void onSaveAs(ActionEvent actionEvent) {
        Window window = this.overlay.getScene().getWindow();
        File file = new FileChooser().showSaveDialog(window);

        if (file != null) {
            saveTo(file);
            this.currentFile = Optional.of(file);
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
            fos.write(Exporter.export(this.overlay.getToplevelPane()).getBytes(Charsets.UTF_8));
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
}
