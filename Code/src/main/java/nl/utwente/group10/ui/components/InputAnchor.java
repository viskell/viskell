package nl.utwente.group10.ui.components;

import javafx.scene.input.MouseEvent;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.gestures.InputAnchorHandler;

import java.io.IOException;
import java.util.Optional;

public class InputAnchor extends ConnectionAnchor {
    private Optional<Connection> up;

    public InputAnchor(Block block, CustomUIPane pane) throws IOException {
        super(block, pane);

        new InputAnchorHandler(this,pane);
        
        up = Optional.empty();

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            up.map(oldAnchor -> {
                return pane.getChildren().removeAll(oldAnchor);
            });

            pane.getLastOutputAnchor().map(anchor -> {
                    Connection upstream = new Connection(anchor, this);
                    pane.getChildren().addAll(upstream);
                    up = Optional.of(upstream);

                return null;
            });

            pane.invalidate();
        });
    }

    public Expr asExpr() {
        if (up.isPresent()) {
            return up.get().getInputFunction().asExpr();
        } else {
            return new Ident("undefined");
        }
    }
    
    public Connection createConnectionFrom(OutputAnchor from){
    	Connection connection = new Connection(from,this);
    	pane.getChildren().add(connection);
    	return connection;
    }
}
