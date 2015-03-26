package nl.utwente.group10.ui;

import nl.utwente.cs.caes.tactile.debug.DebugParent;
import nl.utwente.cs.caes.tactile.fxml.TactileBuilderFactory;
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

		FunctionBlock functionBlock = FunctionBlock.newInstance(2);
		functionBlock.setName("TestTest");
		FunctionBlock functionBlock2 = FunctionBlock.newInstance(1);
		FunctionBlock functionBlock3 = FunctionBlock.newInstance(1);
		FunctionBlock functionBlock4 = FunctionBlock.newInstance(1);

		functionBlock.nest(FunctionBlock.newInstance(1));
		functionBlock.nest(FunctionBlock.newInstance(1));
		functionBlock.nest(FunctionBlock.newInstance(1));
		tactilePane.getChildren().add(functionBlock);
		tactilePane.getChildren().add(functionBlock2);
		tactilePane.getChildren().add(FunctionBlock.newInstance(6));

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
