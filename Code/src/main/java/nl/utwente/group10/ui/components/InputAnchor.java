package nl.utwente.group10.ui.components;

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
        setConnection(null);
    }

    public Expr asExpr() {
        if (up.isPresent()) {
            return up.get().getInputFunction().asExpr();
        } else {
            return new Ident("undefined");
        }
    }
    
    public void setConnection(Connection connection){
    	up = Optional.ofNullable(connection);
    }
    
    public Optional<Connection> getConnection(){
    	return up;
    }
    
    public Connection createConnectionFrom(OutputAnchor from){
    	Connection connection = new Connection(from,this);
    	pane.getChildren().add(connection);
    	return connection;
    }
}
