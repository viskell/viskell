package nl.utwente.group10.ui.components;

import javafx.fxml.FXMLLoader;

/**
 * This interface is responsible for providing a Class specific FXMLLoader to
 * each UI Component.
 */
public interface ComponentLoader {

    default FXMLLoader getFXMLLoader(String name) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(String.format("/ui/%s.fxml", name)));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        return fxmlLoader;
    }
}
