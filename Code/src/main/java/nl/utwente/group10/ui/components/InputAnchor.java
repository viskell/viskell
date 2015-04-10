package nl.utwente.group10.ui.components;

import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.handlers.InputAnchorHandler;

import java.io.IOException;
import java.util.Optional;

public class InputAnchor extends ConnectionAnchor {

	public InputAnchor(Block block, CustomUIPane pane) throws IOException {
		super(block, pane);
		new InputAnchorHandler(pane.getConnectionCreationManager(),this);
	}

	public Expr asExpr() {
		if (isConnected()) {
			return getConnection().get().getOutputAnchor().get().getBlock().asExpr();
		} else {
			return new Ident("undefined");
		}
	}

	@Override
	public Optional<Connection> createConnectionWith(ConnectionAnchor other) {
		if(other instanceof OutputAnchor){
			return createConnectionFrom((OutputAnchor) other);
		}else{
			return Optional.empty();
		}
	}
	
	public Optional<Connection> createConnectionFrom(OutputAnchor other){
		if(!isConnected()){
			new Connection(this, (OutputAnchor) other);
			getPane().getChildren().add(getConnection().get());
			getPane().invalidate();
			return getConnection();
		}else{
			return Optional.empty();
		}
	}
	
	@Override
	public String toString(){
    	return "InputAnchor belonging to "+getBlock().getName();
    }

	@Override
	public boolean allowsConnecting() {
		//InputAnchors only support 1 connection;
		return !isConnected();
	}

	@Override
	public void disconnect(Connection connection) {
		if(connection.equals(getConnection().get())){
			setConnection(null);
		}else{
			throw new MultipleConnectionsException();
		}
	}
	
	public class MultipleConnectionsException extends RuntimeException{}
}
