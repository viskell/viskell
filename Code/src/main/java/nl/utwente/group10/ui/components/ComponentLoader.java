package nl.utwente.group10.ui.components;

import javafx.fxml.FXMLLoader;

import java.io.IOException;

/**
 * This interface is responsible for providing a Class specific FXMLLoader
 * to each UI Component.
 */
public interface ComponentLoader {
    default void loadFXML(String name) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(String.format("/ui/%s.fxml", name)));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);

            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("A required FXML file, " + name + ", could not be loaded.", e);
        }
    }
}
