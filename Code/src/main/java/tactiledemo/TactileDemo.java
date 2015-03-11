package tactiledemo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import nl.utwente.cs.caes.tactile.control.TactilePane;
import nl.utwente.cs.caes.tactile.debug.DebugParent;
import nl.utwente.cs.caes.tactile.fxml.TactileBuilderFactory;

public class TactileDemo extends Application {
    DebugParent debug;
    
    @Override
    public void start(Stage stage) throws Exception {
        BorderPane root = new BorderPane();
        
        // Init TactilePane
        TactilePane tactilePane = (TactilePane) FXMLLoader.load(getClass().getResource("Main.fxml"), null, new TactileBuilderFactory());
        
        // Init Control Pane
        FlowPane controlLayout = new FlowPane();
        CheckBox enableDebug = new CheckBox("Enable Debug Mode");
        enableDebug.setSelected(false);
        controlLayout.getChildren().add(enableDebug);
        
        root.setCenter(tactilePane);
        root.setBottom(controlLayout);
        
        // Init Debug
        debug = new DebugParent(root);
        debug.overlayVisibleProperty().bindBidirectional(enableDebug.selectedProperty());
        debug.registerTactilePane(tactilePane);
        
        // Key bindings
        debug.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
        	@Override
        	public void handle(KeyEvent keyEvent){
                if (keyEvent.getCode() == KeyCode.F11) {
                	if (stage.isFullScreen()){
                		stage.setFullScreen(false);
                	} else {
                		stage.setFullScreen(true);
                	}
                	keyEvent.consume();
                }
            }
        	
        });
        
        Scene scene = new Scene(debug);
        stage.setFullScreen(true);
        stage.setOnCloseRequest(event -> { Platform.exit(); });
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
