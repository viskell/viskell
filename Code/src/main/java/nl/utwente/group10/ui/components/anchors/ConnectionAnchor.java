package nl.utwente.group10.ui.components.anchors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.lines.Connection;

/**
 * Represent an Anchor point on either a Block or a Line Integers are currently
 * the only supported data type.
 * <p>
 * Other data types will be supported in the future.
 * </p>
 */
public abstract class ConnectionAnchor extends Circle implements ComponentLoader {
    /** The pane on which this Anchor resides. */
    private CustomUIPane pane;

    /** The block this Anchor is connected to. */
    private Block block;

    /** The possible connection linked to this anchor */
    private List<Connection> connections;

    /**
     * @param block
     *            The block where this Anchor is connected to.
     * @param pane
     *            The pane this Anchor belongs to.
     */
    public ConnectionAnchor(Block block, CustomUIPane pane) {
        this.block = block;
        this.pane = pane;

        this.loadFXML("ConnectionAnchor");
        connections = new ArrayList<Connection>();
    }

    /**
     * @return Input or output type of the block associated with this anchor.
     */
    public abstract Type getType();

    /**
     * Disconnects the anchor from the connection
     *
     * @param connection
     *            Connection to disconnect from.
     */
    public void removeConnection(Connection connection) {
        if (connections.contains(connection)) {
            connections.remove(connection);
            getPane().getChildren().remove(connection);
            connection.disconnect();
        }
    }
    
    public void disconnectConnection(Connection connection) {
        if (connections.contains(connection)) {
            connections.remove(connection);
            connection.disconnect(this);
        }
    }
    
    public void clearConnections(){
        for (int i = 0; i < getConnections().size(); i++) {
            removeConnection(getConnections().get(0));
        }
    }
    
    public void disconnectConnections(){
        for (int i = 0; i < getConnections().size(); i++) {
            disconnectConnection(getConnections().get(0));
        }
    }

    /**
     * Set the connection this anchor is connected to.
     *
     * @param connection
     */
    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    /** Returns true if the anchor is connected to a connection. */
    public boolean hasConnection() {
        return connections.size() > 0;
    }

    /**
     * @return True if this ConnectionAnchor is connected to a Connection and
     *         that connection is fully connected.
     */
    public boolean isPrimaryConnected() {
        return isConnected(0);
    }
    
    public boolean isConnected(int index){
        return index >= 0 && index < getConnections().size() && getConnections().get(index).isConnected();
    }

    /** Returns true if this anchor can connect to a connection. */
    public abstract boolean canAddConnection();

    /**
     * This method provides a shortcut to get the anchors on the other side of the Connection from this anchor.
     * @return A list of each potential opposite anchor for each Connection this anchor has.
     */
    public List<Optional<? extends ConnectionAnchor>> getOppositeAnchors() {
        List<Optional<? extends ConnectionAnchor>> list = new ArrayList<Optional<? extends ConnectionAnchor>>();
        for(Connection c : getConnections()){
            if(c.isConnected()){
                if(c.getInputAnchor().isPresent() && c.getInputAnchor().get().equals(this)){
                    list.add(c.getOutputAnchor());
                }else if(c.getOutputAnchor().isPresent() && c.getOutputAnchor().get().equals(this)){
                    list.add(c.getInputAnchor());
                }else{
                    list.add(Optional.empty());
                }
            }else{
                list.add(Optional.empty());
            }
        }
        return list;
    }
    
    public Optional<? extends ConnectionAnchor> getPrimaryOppositeAnchor(){
        if(getOppositeAnchors().size()>0){
            return getOppositeAnchors().get(0);
        }else{
            return Optional.empty();
        }
    }

    /** Returns the block this anchor belongs to. */
    public final Block getBlock() {
        return block;
    }

    /** Returns the connection this anchor is connected to. (if any) */
    public List<Connection> getConnections() {
        return connections;
    }
    
    public Optional<Connection> getPrimaryConnection(){
        if(getConnections().size()>0){
            return Optional.of(getConnections().get(0));
        }else{
            return Optional.empty();
        }
    }

    /** Returns the position of the center of this anchor relative to its pane. */
    public Point2D getCenterInPane() {
        Point2D scenePos = localToScene(getCenterX(), getCenterY());
        return getPane().sceneToLocal(scenePos);
    }

    /** Returns the pane this anchor resides on. */
    public final CustomUIPane getPane() {
        return pane;
    }

    @Override
    public String toString() {
        return String.format("%s for %s", this.getClass().getSimpleName(), getBlock());
    }
}
