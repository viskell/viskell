package nl.utwente.group10.ui.components;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.shape.Circle;
import nl.utwente.group10.ui.CustomUIPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Represent an Anchor point on either a Block or a Line
 * Integers are currently the only supported data type.
 * 
 * Other data types will be supported in the future
 */
public abstract class ConnectionAnchor extends Circle implements Initializable {
    /** The fxmlLoader responsible for loading the fxml of this Block.*/
    private FXMLLoader fxmlLoader;

    /** Our parent CustomUIPane. */
    private CustomUIPane pane;

    /** Our parent Block. */
    private Block block;
    
    /** The possible connection linked to this anchor */
	private Optional<Connection> connection;

    public ConnectionAnchor(Block block, CustomUIPane pane) throws IOException {
        this.block = block;
        this.pane = pane;

        fxmlLoader = new FXMLLoader(getClass().getResource("/ui/ConnectionAnchor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);			

        fxmlLoader.load();
        
        setConnection(null);
    }

    public void setConnection(Connection connection){
    	this.connection = Optional.ofNullable(connection);
    }
    
    public boolean isConnected(){
    	return connection.isPresent();
    }
    
    public Optional<Connection> getConnection(){
    	return connection;
    }
    
    public abstract boolean canConnect();
    public abstract void disconnect(Connection connection);
    
    public Optional<ConnectionAnchor> getOtherAnchor(){
    	if(isConnected()){
    		Optional<OutputAnchor> out =getConnection().get().getOutputAnchor();
    		Optional<InputAnchor> in =getConnection().get().getInputAnchor();
    		if(in.isPresent() && out.get().equals(this)){
    			return Optional.of(in.get());
    		}else if(out.isPresent() && in.get().equals(this)){
    			return Optional.of(out.get());
    		}
    	}
    	return Optional.empty();
    }
    
    /**
     * @return The newly made Connection of successful
     * Note that failing to add a connection while already connected will still return an empty optional.
     * (No new Connection was created)
     */    
    public abstract Optional<Connection> createConnectionWith(ConnectionAnchor other);
    
    public Block getBlock() {
        return block;
    }

    public CustomUIPane getPane() {
        return pane;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    
    public FXMLLoader getLoader(){
        return fxmlLoader;
    }
    
    @Override
    public String toString(){
    	return "ConnectionAnchor belonging to "+getBlock().getName();
    }
}
