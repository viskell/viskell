package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.Optional;

import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.type.Type;

/** Variant of the OutputAnchor that maintains a separate type for each connections.
 *  This is used e.g. for the anchor from a lambda block to allow each use site having a different specialized type. 
 */
public class PolyOutputAnchor extends OutputAnchor {

    /** List of type corresponding to each connection */
    private final ArrayList<Type> connectionTypes;
    
    public PolyOutputAnchor(Block block, Binder binder) {
        super(block, binder);
        this.connectionTypes = new ArrayList<>();
    }

    @Override
    public Type getType(Optional<Connection> targetConnection) {
        if (targetConnection.isPresent()) {
            int index = this.connections.indexOf(targetConnection.get());
            return this.connectionTypes.get(index);
        }
        
        return this.binder.getBoundType();
    }

    @Override
    protected void addConnection(Connection connection) {
        super.addConnection(connection);
        this.connectionTypes.add(this.binder.getBoundType().getFresh());
    }

    @Override
    protected void dropConnection(Connection connection) {
        int index = this.connections.indexOf(connection);
        super.dropConnection(connection);
        if (index >= 0) {
            this.connectionTypes.remove(index);
        }
    }
    
    @Override
    public void removeConnections() {
        super.removeConnections();
        this.connectionTypes.clear();
    }

    public void setExactRequiredType(Type type) {
        super.setExactRequiredType(type);
        for (int i = 0; i < this.connectionTypes.size(); i++) {
            this.connectionTypes.set(i, this.binder.getBoundType().getFresh());
        }
    }

}
