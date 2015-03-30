package nl.utwente.group10.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import nl.utwente.ewi.caes.tactilefx.debug.DebugParent;
import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;
import nl.utwente.group10.haskell.catalog.Entry;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.ui.components.Connection;
import nl.utwente.group10.ui.components.DisplayBlock;
import nl.utwente.group10.ui.components.FunctionBlock;
import nl.utwente.group10.ui.components.ValueBlock;

public class Main extends Application {
	private DebugParent debug;
	private CustomUIPane tactilePane;

	@Override
	public void start(Stage stage) throws Exception {
		BorderPane root = new BorderPane();

		tactilePane = FXMLLoader.load(this.getClass().getResource("/ui/Main.fxml"), null, new TactileBuilderFactory());

		HaskellCatalog catalog = new HaskellCatalog();

		Entry plus = catalog.getEntry("(+)");
		FunctionBlock plusBlock = new FunctionBlock(plus.getName(), plus.getType(), tactilePane);
		tactilePane.getChildren().add(plusBlock);

		Entry id = catalog.getEntry("id");
		FunctionBlock idBlock = new FunctionBlock(id.getName(), id.getType(), tactilePane);
		tactilePane.getChildren().add(idBlock);

		Entry pi = catalog.getEntry("pi");
		FunctionBlock piBlock = new FunctionBlock(pi.getName(), pi.getType(), tactilePane);
		tactilePane.getChildren().add(piBlock);

		tactilePane.getChildren().add(new ValueBlock("6.0"));
		tactilePane.getChildren().add(new DisplayBlock());
		
		Connection connection = new Connection(idBlock, idBlock.getOutputAnchor(), plusBlock, plusBlock.getInputs()[0]);
		tactilePane.getChildren().add(connection);

		// Init Control Pane
		FlowPane controlLayout = new FlowPane();
		CheckBox enableDebug = new CheckBox("Enable Debug Mode");
		enableDebug.setSelected(false);
		controlLayout.getChildren().add(enableDebug);

		root.setCenter(tactilePane);
		root.setBottom(controlLayout);

		// Init Debug
		debug = new DebugParent(root);
		debug.overlayVisibleProperty().bindBidirectional(
				enableDebug.selectedProperty());
		debug.registerTactilePane(tactilePane);

		Scene scene = new Scene(debug);
		stage.setOnCloseRequest(event -> System.exit(0));
		stage.setScene(scene);
		stage.show();

		// Invalidate
		invalidate();
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
