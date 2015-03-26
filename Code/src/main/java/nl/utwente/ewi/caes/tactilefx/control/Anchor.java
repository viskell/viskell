package nl.utwente.ewi.caes.tactilefx.control;

import javafx.geometry.Pos;
import javafx.scene.Node;

public class Anchor {
    private final Node anchorNode;
    private double offsetX;
    private double offsetY;
    private Pos alignment;

    public Anchor(Node anchorNode) {
        this(anchorNode, 0, 0, null);
    }

    public Anchor(Node anchorNode, double offsetX, double offsetY) {
        this(anchorNode, offsetX, offsetY, null);
    }

    public Anchor(Node anchorNode, Pos alignment) {
        this(anchorNode, 0, 0, alignment);
    }

    public Anchor(Node anchorNode, double offsetX, double offsetY, Pos alignment) {
        if (anchorNode == null) {
            throw new NullPointerException("anchorNode may not be null");
        }

        this.anchorNode = anchorNode;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.alignment = (alignment == null) ? Pos.TOP_LEFT : alignment;
    }

    public Node getAnchorNode() {
        return anchorNode;
    }
    
    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }
    
    public double getOffsetX() {
        return offsetX;
    }
    
    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public double getOffsetY() {
        return offsetY;
    }
    
    public void setAlignment(Pos alignment) {
        this.alignment = alignment;
    }

    public Pos getAlignment() {
        return alignment;
    }
}