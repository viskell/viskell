package nl.utwente.group10.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.stage.Stage;
import nl.utwente.ewi.caes.tactilefx.debug.DebugParent;
import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.ui.components.DisplayBlock;
import nl.utwente.group10.ui.components.ValueBlock;

/**
 * Main application class for the GUI.
 */
public class Main extends Application {
	/** Pane that is used for outputting debug information about touch interactions and user interface elements. */
	private DebugParent debug;

	/** Primary pane which contains blocks and connections. */
	private CustomUIPane tactilePane;

	@Override
	public void start(Stage stage) throws Exception {
		tactilePane = FXMLLoader.load(this.getClass().getResource("/ui/Main.fxml"), null, new TactileBuilderFactory());

		HaskellCatalog catalog = new HaskellCatalog();

		tactilePane.getChildren().add(new ValueBlock(tactilePane));
		tactilePane.getChildren().add(new DisplayBlock(tactilePane));

		// Init Debug
		debug = new DebugParent(tactilePane);
		debug.registerTactilePane(tactilePane);
		debug.setOverlayVisible(false);

		// Init menu
		ContextMenu menu = new MainMenu(catalog, tactilePane);
		tactilePane.setContextMenu(menu);

		// Init scene
		Scene scene = new Scene(debug);
		stage.setOnCloseRequest(event -> System.exit(0));
		stage.setScene(scene);
		stage.show();

		// Invalidate
		tactilePane.invalidate();
	}

	/**
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
