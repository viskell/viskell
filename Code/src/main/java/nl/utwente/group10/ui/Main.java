package nl.utwente.group10.ui;

import nl.utwente.ewi.caes.tactilefx.debug.DebugParent;
import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;
import nl.utwente.group10.ui.components.FunctionBlock;
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

		FunctionBlock functionBlock = FunctionBlock.newInstance(2, tactilePane);
		functionBlock.setName("TestTest");
		FunctionBlock functionBlock2 = FunctionBlock.newInstance(0, tactilePane);
		FunctionBlock functionBlock3 = FunctionBlock.newInstance(0, tactilePane);
		FunctionBlock functionBlock4 = FunctionBlock.newInstance(0, tactilePane);

		functionBlock4.nest(FunctionBlock.newInstance(0, tactilePane));
		functionBlock3.nest(FunctionBlock.newInstance(0, tactilePane));
		functionBlock3.nest(FunctionBlock.newInstance(0, tactilePane));
		functionBlock2.nest(functionBlock4);
		functionBlock2.nest(functionBlock3);

		functionBlock.nest(FunctionBlock.newInstance(0, tactilePane));
		functionBlock.nest(FunctionBlock.newInstance(0, tactilePane));
		functionBlock.nest(FunctionBlock.newInstance(0, tactilePane));
		tactilePane.getChildren().add(functionBlock);
		tactilePane.getChildren().add(functionBlock2);
		tactilePane.getChildren().add(FunctionBlock.newInstance(0, tactilePane));

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
