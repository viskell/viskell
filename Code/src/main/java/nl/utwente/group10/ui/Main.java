package nl.utwente.group10.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import nl.utwente.ewi.caes.tactilefx.debug.DebugParent;
import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;
import nl.utwente.group10.haskell.catalog.Entry;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.ui.components.DisplayBlock;
import nl.utwente.group10.ui.components.FunctionBlock;
import nl.utwente.group10.ui.components.ValueBlock;

import java.io.IOException;

public class Main extends Application {
	private DebugParent debug;
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

		// Init menu
		ContextMenu menu = new ContextMenu();

		Menu addFunc = new Menu("Add function...");
		for (String category : catalog.getCategories()) {
			Menu submenu = new Menu(category);

			for (Entry entry : catalog.getCategory(category)) {
				MenuItem item = new MenuItem(entry.getName());
				item.setOnAction(event -> addFunctionBlock(entry));
				submenu.getItems().add(item);
			}

			addFunc.getItems().addAll(submenu);
		}

		CheckMenuItem enableDebugItem = new CheckMenuItem("Debug mode");
		enableDebugItem.selectedProperty().bindBidirectional(debug.overlayVisibleProperty());
		enableDebugItem.setSelected(false);

		MenuItem quitItem = new MenuItem("Quit");
		quitItem.setOnAction(event -> System.exit(0));

		menu.getItems().addAll(addFunc, enableDebugItem, quitItem);
		tactilePane.setContextMenu(menu);

		// Init scene
		Scene scene = new Scene(debug);
		stage.setOnCloseRequest(event -> System.exit(0));
		stage.setScene(scene);
		stage.show();

		// Invalidate
		invalidate();
	}

	private void addFunctionBlock(Entry entry) {
		try {
			FunctionBlock fb = new FunctionBlock(entry.getName(), entry.getType(), tactilePane);
			tactilePane.getChildren().add(fb);
		} catch (IOException e) {
			panic(e);
		}
	}

	private void panic(Exception e) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Exception");
		alert.setHeaderText("An unexpected error occurred.");
		alert.setContentText(String.format("%s", e));
		alert.showAndWait();
	}

	/** Re-evaluate all displays. */
	private void invalidate() {
		for (Node node : tactilePane.getChildren()) {
			if (node instanceof DisplayBlock) {
				((DisplayBlock)node).invalidate();
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
