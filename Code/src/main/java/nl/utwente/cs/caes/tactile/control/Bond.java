package nl.utwente.cs.caes.tactile.control;

import javafx.scene.Node;

public class Bond {
    private final Node bondNode;
    private double distance;
    private double forceMultiplier;
    
    /**
     * Initializes a new {@code Bond}.
     * @param bondNode  The Node that should be followed
     * @param distance  The distance from the bondNode that should be aimed for
     * @param forceMultiplier Multiplied with the vector that is the result of
     * this Bond. Used to control the speed with which a Node will follow the {@code bondNode}
     */
    public Bond(Node bondNode, double distance, double forceMultiplier) {
        this.bondNode = bondNode;
        this.distance = distance;
        this.forceMultiplier = forceMultiplier;
    }
    
    /**
     * Initializes a new {@code Bond} with a default {@code forceMultiplier} of 0.5.
     * @param bondNode  The Node that should be followed
     * @param distance  The distance from the bondNode that should be aimed for
     */
    public Bond(Node bondNode, double distance) {
        this(bondNode, distance, 0.5);
    }
    
    public Node getBondNode() {
        return bondNode;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public double getDistance() {
        return distance;
    }

    public void setForceMultiplier(double forceMultiplier) {
        this.forceMultiplier = forceMultiplier;
    }
    
    public double getForceMultiplier() {
        return forceMultiplier;
    }
}
