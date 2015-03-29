package nl.utwente.group10.ui;

import nl.utwente.ewi.caes.tactilefx.debug.DebugParent;
import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;
import nl.utwente.group10.ui.components.Connection;
import nl.utwente.group10.ui.components.ConnectionAnchor;
import nl.utwente.group10.ui.components.DisplayBlock;
import nl.utwente.group10.ui.components.FunctionBlock;
import nl.utwente.group10.ui.components.ValueBlock;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class Main extends Application {
	DebugParent debug;

	@Override
	public void start(Stage stage) throws Exception {
		BorderPane root = new BorderPane();

		CustomUIPane tactilePane = FXMLLoader.load(this.getClass().getResource("/ui/Main.fxml"), null, new TactileBuilderFactory());

		DisplayBlock displayBlock = new DisplayBlock();
		FunctionBlock functionBlock = new FunctionBlock(2, tactilePane);
		ValueBlock valueBlock = new ValueBlock("6");
		ValueBlock valueBlock2 = new ValueBlock("6");
		
		tactilePane.getChildren().add(functionBlock);
		tactilePane.getChildren().add(displayBlock);
		tactilePane.getChildren().add(valueBlock);
		tactilePane.getChildren().add(valueBlock2);

		Connection connection = new Connection(functionBlock, functionBlock.getOutputAnchor(), displayBlock, displayBlock.getInputAnchor());
		Connection connection2 = new Connection(valueBlock, valueBlock.getOutputAnchor(), functionBlock, functionBlock.getInputs()[0]);
		Connection connection3 = new Connection(valueBlock2, valueBlock2.getOutputAnchor(), functionBlock, functionBlock.getInputs()[1]);
		
		tactilePane.getChildren().add(connection);
		tactilePane.getChildren().add(connection2);
		tactilePane.getChildren().add(connection3);


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
		stage.setOnCloseRequest(event -> {
			Platform.exit();
		});
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
