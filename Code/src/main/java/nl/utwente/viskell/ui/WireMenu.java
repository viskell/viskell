package nl.utwente.viskell.ui;

import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;
import javafx.util.Duration;
import nl.utwente.viskell.ui.components.*;

/** A menu for attaching something to an open wire. */
public class WireMenu extends TilePane {

    /** The toplevel pane this menu is on. */
    private final ToplevelPane toplevel;
    
    /** The draw wire belonging to this menu */
    private DrawWire attachedWire;
    
    public WireMenu(DrawWire wire) {
        this.toplevel = wire.getAnchor().getPane();
        this.attachedWire = wire;
        this.setMouseTransparent(true);

        this.getStyleClass().add("menu");
        this.setPrefColumns(1);
        this.setVgap(5);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> this.attachedWire.remove());
        // FIXME: silly workaround, somehow the buttons in this menu don't work on touch click while those in FunctionMenu do...
        cancelButton.setOnTouchPressed(event -> this.attachedWire.remove());
        
        if (wire.getAnchor() instanceof InputAnchor) {
            Button lambdaBlockButton = new Button("Lambda");
            lambdaBlockButton.setOnAction(event -> addBlockWithOutput(new DefinitionBlock(this.toplevel, 1)));
            lambdaBlockButton.setOnTouchPressed(event -> addBlockWithOutput(new DefinitionBlock(this.toplevel, 1)));
            
            Button rationalBlockButton = new Button("Rational");
            rationalBlockButton.setOnAction(event -> addBlockWithOutput(new SliderBlock(this.toplevel, false)));
            rationalBlockButton.setOnTouchPressed(event -> addBlockWithOutput(new SliderBlock(this.toplevel, false)));
            
            Button IntegerBlockButton = new Button("Integer");
            IntegerBlockButton.setOnAction(event -> addBlockWithOutput(new SliderBlock(this.toplevel, true)));
            IntegerBlockButton.setOnTouchPressed(event -> addBlockWithOutput(new SliderBlock(this.toplevel, true)));

            this.getChildren().addAll(cancelButton, lambdaBlockButton, rationalBlockButton, IntegerBlockButton);
            
        } else {
            Button disBlockButton = new Button("Display");
            disBlockButton.setOnAction(event -> addBlockWithInput(new DisplayBlock(this.toplevel)));
            disBlockButton.setOnTouchPressed(event -> addBlockWithInput(new DisplayBlock(this.toplevel)));
            
            Button graphBlockButton = new Button("Graph");
            graphBlockButton.setOnAction(event -> addBlockWithInput(new GraphBlock(this.toplevel)));
            graphBlockButton.setOnTouchPressed(event -> addBlockWithInput(new GraphBlock(this.toplevel)));

            this.getChildren().addAll(cancelButton, disBlockButton, graphBlockButton);
        }

        // opening animation
        this.setScaleX(0.1);
        this.setScaleY(0.1);
        ScaleTransition opening = new ScaleTransition(Duration.millis(250), this);
        opening.setToX(1);
        opening.setToY(1);
        opening.setOnFinished(e -> this.setMouseTransparent(false));
        opening.play();
    }
    
    private void addBlockWithInput(Block block) {
        this.toplevel.addBlock(block);
        block.relocate(this.attachedWire.getEndX(), this.attachedWire.getEndY());
        this.close();
        
        block.initiateConnectionChanges();
        InputAnchor input = block.getAllInputs().get(0);
        Connection connection = this.attachedWire.buildConnectionTo(input);
        if (connection != null) {
            connection.getStartAnchor().initiateConnectionChanges();
        }
        this.attachedWire.remove();
    }

    private void addBlockWithOutput(Block block) {
        this.toplevel.addBlock(block);
        block.relocate(this.attachedWire.getEndX(), this.attachedWire.getEndY() - block.prefHeight(-1));
        this.close();
        
        block.initiateConnectionChanges();
        OutputAnchor output = block.getAllOutputs().get(0);
        Connection connection = this.attachedWire.buildConnectionTo(output);
        if (connection != null) {
            connection.getStartAnchor().initiateConnectionChanges();
        }
        this.attachedWire.remove();
    }

    /** Closes this menu by removing it from it's parent. */
    public void close() {
        this.toplevel.removeMenu(this);
    }
    
}
