package nl.utwente.viskell.ui.components;

import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;

/**
 * Provides a default method for loading FXML interface description files.
 *
 * The ComponentLoader is implemented as an interface so it can be added to
 * other classes without forcing those classes to inherit from it.
 *
 * loadFXML will throw a RuntimeException when loading the FXML file fails,
 * which should never happen as it will only load from resources.
 */
public interface ComponentLoader {
    default void loadFXML(String name) {
        try {
            URL url = getClass().getResource(String.format("/ui/%s.fxml", name));
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);

            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("A required FXML file, " + name + ", could not be loaded.", e);
        }
    }
}
