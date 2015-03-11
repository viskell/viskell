package lwbdemo;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import lwbdemo.ui.Bowtie;
import lwbdemo.model.Function;
import lwbdemo.model.List;
import lwbdemo.model.VariableType;
import lwbdemo.model.ConstantType;
import nl.utwente.cs.caes.tactile.control.TactilePane;
import nl.utwente.cs.caes.tactile.debug.DebugParent;

public class Main extends Application {
    TactilePane tactilePane;
    BorderPane root;
    DebugParent debug;
    
    @Override
    public void start(Stage stage) throws Exception {
        tactilePane = new TactilePane();
        root = new BorderPane();
        debug = new DebugParent(root);
        
        // map bowtie
        VariableType a = new VariableType("a");
        VariableType b = new VariableType("b");
        Bowtie btFuncMap = new Bowtie(tactilePane, "map", new Function(a, b), new List(a), new List(b));
        // cost bowtie
        VariableType c = new VariableType("c");
        VariableType d = new VariableType("d");
        Bowtie btFuncConst = new Bowtie(tactilePane, "const", c, d, c);
        // length bowtie
        VariableType e = new VariableType("e");
        Bowtie btFuncLength = new Bowtie(tactilePane, "length", new List(e), ConstantType.INT);
        // add bowtie
        Bowtie btFuncAdd = new Bowtie(tactilePane, "add", ConstantType.INT, ConstantType.INT, ConstantType.INT);
        // "foo" bowtie
        Bowtie btStringFoo = new Bowtie(tactilePane, "\"foo\"", new List(ConstantType.CHAR));
        // 1 bowtie
        Bowtie btInt1 = new Bowtie(tactilePane, "1", ConstantType.INT);
        
        tactilePane.setBordersCollide(true);
        tactilePane.setDragProcessingMode(TactilePane.EventProcessingMode.HANDLER);
        tactilePane.getChildren().addAll(btFuncMap, btFuncConst, btFuncLength, btFuncAdd, btStringFoo, btInt1);
        double x = 10; double y = 10;
        for (Node child : tactilePane.getChildren()) {
            TactilePane.setSlideOnRelease(child, true);
            child.relocate(x, y);
            x += 50;
            y += 50;
        }
        
        // Toggle for Debug.setOverlayVisible
        CheckBox debugCheckBox = new CheckBox("Debug");
        debug.overlayVisibleProperty().bind(debugCheckBox.selectedProperty());
        
        // Button for clearing touchpoints
        Button clearTouchPointsButton = new Button("Clear TouchPoints");
        clearTouchPointsButton.visibleProperty().bind(debug.overlayVisibleProperty());
        clearTouchPointsButton.setOnAction(event -> {
            debug.clearTouchPoints();
        });
        
        root.setCenter(tactilePane);
        root.setBottom(new FlowPane(debugCheckBox, new Separator(Orientation.VERTICAL), clearTouchPointsButton));
        
        debug.registerTactilePane(tactilePane);
        
        Scene scene = new Scene(debug, 800, 600);
        
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        Main.launch(args);
    }
}
