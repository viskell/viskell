package nl.utwente.group10.ui.handlers;

import java.util.HashMap;
import java.util.Map;


import java.util.Optional;

import javafx.geometry.Point2D;
import javafx.scene.Parent;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.Connection;
import nl.utwente.group10.ui.components.ConnectionAnchor;
import nl.utwente.group10.ui.components.ConnectionLine;
import nl.utwente.group10.ui.components.InputAnchor;
import nl.utwente.group10.ui.components.OutputAnchor;

public class ConnectionCreationManager {

	CustomUIPane pane;

	/**
	 * Touch points have an ID associated with each specific touch point, this
	 * is the ID associated with the Mouse.
	 */
	public static final Integer MOUSE_ID = 0;
	/**
	 * Maps an (Touch or Mouse) ID to a line, used to keep track of what touch
	 * point is dragging what line.
	 */
	private Map<Integer, Connection> connections;
	
	
	public ConnectionCreationManager(CustomUIPane pane){
		this.pane = pane;
		connections = new HashMap<Integer, Connection>();
	}
	

	public Connection createConnectionWith(int id, ConnectionAnchor anchor) {
		Connection newConnection = null;
		if(anchor instanceof OutputAnchor){
			newConnection = new Connection((OutputAnchor) anchor);
		} else if(anchor instanceof InputAnchor){
			newConnection = new Connection((InputAnchor) anchor);
		}
		pane.getChildren().add(newConnection);
		anchor.startFullDrag();
		connections.put(id,newConnection);
		return newConnection;
	}
	
	public Connection finalizeConnection(int id, ConnectionAnchor anchor){
		Connection connection = connections.get(id);
		if(connection!=null){
			if(anchor.canConnect() && connection.addAnchor(anchor)){
				pane.invalidate();
			}else{
				finalizeConnection(id);
			}
		}
		connections.put(id, null);
		return connection;		
	}
	public Connection finalizeConnection(int id){
		Connection connection = connections.get(id);
		connections.put(id, null);
		if(connection!=null){
			connection.disconnect();
			pane.getChildren().remove(connection);
		}
		return null;		
	}
	
	public void editConnection(int id, ConnectionAnchor anchor) {
		Optional<ConnectionAnchor> anchorToKeep = anchor.getOtherAnchor();
		if(anchor.isConnected() && anchorToKeep.isPresent()){
			Connection connection = anchor.getConnection().get(); 
			connection.disconnect(anchor);
			anchorToKeep.get().startFullDrag();
			connections.put(id, connection);
			pane.invalidate();
		}
	}

	public void updateLine(int id, double x, double y) {
		if(connections.get(id)!=null){
			connections.get(id).setFreeEnds(x, y);
		}
	}
}
