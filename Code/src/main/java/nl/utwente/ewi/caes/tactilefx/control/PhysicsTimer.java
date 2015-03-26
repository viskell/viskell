package nl.utwente.ewi.caes.tactilefx.control;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javafx.animation.AnimationTimer;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import nl.utwente.ewi.caes.tactilefx.event.TactilePaneEvent;

class PhysicsTimer extends AnimationTimer {
    // Length of a time step
    protected static final double TIME_STEP = 1d / 60d;
    // Default value for force
    protected static final double DEFAULT_FORCE = 100;
    
    final TactilePane pane;
    final ConcurrentHashMap<Node, Point2D> locationByNode  = new ConcurrentHashMap<>();

    PhysicsTimer(TactilePane tactilePane) {
        this.pane = tactilePane;
    }
    
    private double accumulatedTime;
    private long previousTime = 0;
    
    @Override 
    public void handle(long currentTime) {
        if (previousTime == 0) {
            previousTime = currentTime;
            return;
        }

        double secondsEllapsed = (currentTime - previousTime) / 1e9d;
        accumulatedTime += secondsEllapsed;
        previousTime = currentTime;

        while (accumulatedTime >= TIME_STEP) {
            updatePositions();
            checkCollisions();
            accumulatedTime -= TIME_STEP;
        }
    }
    
    // LAYOUT METHODS
    
    private void updatePositions() {
        // Copy children to new list, so we don't get a ConcurrentModificationException when calling toFront()
        List<Node> children = new ArrayList<>();
        pane.getChildren().stream()
                .filter(TactilePane::isDraggable)
                .forEach(children::add);
        
        for (Node node: children) {
            Point2D vector = TactilePane.getVector(node);
            
            // Multiply with FRICTION to model friction
            vector = vector.multiply(pane.getFrictionMultiplier());
            TactilePane.setVector(node, vector);
            
            // If the resulting vector is small enough, set the vector to zero vector
            if (Math.abs(vector.magnitude()) < pane.getVectorThreshold()) {
                vector = Point2D.ZERO;
                TactilePane.setVector(node, vector);
            }
            
            Anchor anchor = TactilePane.getAnchor(node);
            // If the node is in use, update its vector for slide behaviour
            if (TactilePane.isInUse(node) && TactilePane.isSlideOnRelease(node)) {
                Point2D prevLocation = locationByNode.get(node);
                
                // Calculate change in position
                double deltaX = node.getLayoutX() - prevLocation.getX();
                double deltaY = node.getLayoutY() - prevLocation.getY();
                
                // Update vector
                Point2D newVector = TactilePane.getVector(node).add(new Point2D(deltaX , deltaY).multiply(pane.getSlideMultiplier()));
                TactilePane.setVector(node, newVector);
            }
            // Update vector for Bonds
            for (Bond bond : TactilePane.getBonds(node)) {
                Node other = bond.getBondNode();
                if (other == node) continue;
                
                Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());
                Bounds otherBounds = other.localToScene(other.getBoundsInLocal());

                double nodeX = nodeBounds.getMinX() + nodeBounds.getWidth() / 2;
                double nodeY = nodeBounds.getMinY() + nodeBounds.getHeight() / 2;
                double otherX = otherBounds.getMinX() + otherBounds.getWidth() / 2;
                double otherY = otherBounds.getMinY() + otherBounds.getHeight() / 2;
                
                Point2D distance = new Point2D(nodeX - otherX, nodeY - otherY);
                
                TactilePane.setVector(node, TactilePane.getVector(node).add(distance.normalize().multiply(- bond.getForceMultiplier()*(distance.magnitude() - bond.getDistance()))));
                
            }
            
            // If the node is not actively being used and not anchored update the node's position according to vector
            if (!TactilePane.isInUse(node) && anchor == null && !vector.equals(Point2D.ZERO)) {
                layoutNode(node, vector.multiply(TIME_STEP));
            }
            // If anchored, update the node's position according to its anchor
            else if (anchor != null) {
                Node anchorNode = anchor.getAnchorNode();
                Bounds anchorBounds = pane.sceneToLocal(anchorNode.localToScene(anchorNode.getBoundsInLocal()));
                Bounds nodeBounds = node.getBoundsInParent();
                
                // Relocate anchored Node
                double x = anchor.getOffsetX(); 
                double y = anchor.getOffsetY();
                switch(anchor.getAlignment().getHpos()) {
                    case LEFT: 
                        x += anchorBounds.getMinX();
                        break;
                    case CENTER:
                        x += anchorBounds.getMinX() + anchorBounds.getWidth() / 2 - nodeBounds.getWidth() / 2;
                        break;
                    case RIGHT:
                        x += anchorBounds.getMaxX() - nodeBounds.getWidth();
                        break;
                }
                switch(anchor.getAlignment().getVpos()) {
                    case TOP:
                        y += anchorBounds.getMinY();
                        break;
                    case CENTER:
                        y += anchorBounds.getMinY() + anchorBounds.getHeight() / 2 - nodeBounds.getHeight() / 2;
                        break;
                    case BOTTOM:
                        y += anchorBounds.getMaxY() - nodeBounds.getHeight();
                        break;
                    case BASELINE:
                        // TODO Support Baseline alignment?
                        break;
                }
                node.setLayoutX(x);
                node.setLayoutY(y);
                
                // Only call toFront if necessary
                while(anchorNode != null && !pane.getChildren().contains(anchorNode)) {
                    anchorNode = anchorNode.getParent();
                }
                if (anchorNode != null && pane.getChildren().indexOf(node) < pane.getChildren().indexOf(anchorNode)) {
                    node.toFront();
                }
                
            }
            
            // Record the new location
            locationByNode.put(node, new Point2D(node.getLayoutX(), node.getLayoutY()));
        }
    }

    /**
     * Relocates a given Node by delta. If the TactilePane's bordersCollide property 
     * is set to true, it will be ensured that the Node won't be relocated outside of
     * the TactilePane's bounds. If a Node collides with the border of its TactilePane,
     * it will get a new vector that is the reflection of its current one, to simulate
     * reflection.
     */
    private void layoutNode(Node node, Point2D delta) {
        double deltaX = delta.getX();
        double deltaY = delta.getY();
        
        if (!pane.isBordersCollide()) {
            // Using setLayoutX/setLayoutY instead of relocate, relocate acts strange for Circles
            node.setLayoutX(node.getLayoutX() + deltaX);
            node.setLayoutY(node.getLayoutY() + deltaY);
            return;
        }
        
        Bounds paneBounds = pane.getBoundsInLocal();
        Bounds nodeBounds = node.getBoundsInParent();

        // The bounds this node would get when it would be translated by deltaX and deltaY
        Bounds destination = new BoundingBox(nodeBounds.getMinX() + deltaX, nodeBounds.getMinY() + deltaY, nodeBounds.getWidth(), nodeBounds.getHeight());
        
        if (paneBounds.contains(destination)) {
            node.setLayoutX(node.getLayoutX() + deltaX);
            node.setLayoutY(node.getLayoutY() + deltaY);
        } else {
            Point2D trimmedDelta1 = null;   // Delta trimmed so that it stops at the vertical wall the node would collide with (null if there's no such wall)
            Point2D trimmedDelta2 = null;   // Delta trimmed so that it stops at the horizontal wall the node would collide with (null if there's no such wall)
            Point2D restDelta;              // delta - td, with td = minimum(td1, td2)
            Point2D reflectionDelta;        // The delta that is the result of bouncing off a wall
            
            double xyRatio = deltaX / deltaY;
            
            // Compute td1 if one exists
            if (deltaX < 0 && destination.getMinX() < paneBounds.getMinX()) {
                deltaX = paneBounds.getMinX() - nodeBounds.getMinX();
                deltaY = deltaX / xyRatio;
                trimmedDelta1 = new Point2D(deltaX, deltaY);
            } else if (deltaX > 0 && destination.getMaxX() > paneBounds.getMaxX()) {
                deltaX = paneBounds.getMaxX() - nodeBounds.getMaxX();
                deltaY = deltaX / xyRatio;
                trimmedDelta1 = new Point2D(deltaX, deltaY);
            }
            // Compute td2 if one exists
            if (deltaY < 0 && destination.getMinY() < paneBounds.getMinY()) {
                deltaY = paneBounds.getMinY() - nodeBounds.getMinY();
                deltaX = deltaY * xyRatio;
                trimmedDelta2 = new Point2D(deltaX, deltaY);
            } else if (deltaY > 0 && destination.getMaxY() > paneBounds.getMaxY()) {
                deltaY = paneBounds.getMaxY() - nodeBounds.getMaxY();
                deltaX = deltaY * xyRatio;
                trimmedDelta2 = new Point2D(deltaX, deltaY);
            }
            // Determine reflectionDelta
            if (trimmedDelta1 == null || (trimmedDelta2 != null && trimmedDelta1.magnitude() > trimmedDelta2.magnitude())) {
                // Would hit top/bottom border before left/right
                restDelta = delta.subtract(trimmedDelta2);
                reflectionDelta = new Point2D(restDelta.getX(), -restDelta.getY());
            } else {
                // Would hit left/right border before top/bottom
                restDelta = delta.subtract(trimmedDelta1);
                reflectionDelta = new Point2D(-restDelta.getX(), restDelta.getY());
            }
            
            // Relocate node to the wall it collides with
            node.setLayoutX(node.getLayoutX() + deltaX);
            node.setLayoutY(node.getLayoutY() + deltaY);
            TactilePane.setVector(node, reflectionDelta.multiply(1 / TIME_STEP).multiply(pane.getBounceMultiplier()));
            
            // Layout the node for the remaining delta
            layoutNode(node, reflectionDelta);
        }
    }
    
    private void checkCollisions() {
        // Update QuadTree
        pane.quadTree.update();

        for (Node thisNode : pane.getActiveNodes()) {
            Bounds thisBounds = thisNode.localToScene(thisNode.getBoundsInLocal());
            Bounds proximityBounds = null;
            
            double pt = pane.getProximityThreshold();
            if (pt > 0) {
                double x = thisBounds.getMinX() - pt;
                double y = thisBounds.getMinY() - pt;
                double w = thisBounds.getWidth() + pt * 2;
                double h = thisBounds.getHeight() + pt * 2;
                proximityBounds = new BoundingBox(x, y, w, h);
            }

            List<Node> otherNodes = pane.quadTree.retrieve(thisNode);
            for (Node otherNode : otherNodes) {
                Bounds otherBounds = otherNode.localToScene(otherNode.getBoundsInLocal());

                if (thisBounds.intersects(otherBounds)) {
                    if (TactilePane.getNodesColliding(thisNode).add(otherNode)) {
                        TactilePane.getNodesColliding(otherNode).add(thisNode);

                        thisNode.fireEvent(new TactilePaneEvent(TactilePaneEvent.AREA_ENTERED, thisNode, otherNode));
                        otherNode.fireEvent(new TactilePaneEvent(TactilePaneEvent.AREA_ENTERED, otherNode, thisNode));
                    }
                    thisNode.fireEvent(new TactilePaneEvent(TactilePaneEvent.IN_AREA, thisNode, otherNode));
                } else {
                    if (TactilePane.getNodesColliding(thisNode).remove(otherNode)) {
                        TactilePane.getNodesColliding(otherNode).remove(thisNode);

                        thisNode.fireEvent(new TactilePaneEvent(TactilePaneEvent.AREA_LEFT, thisNode, otherNode));
                        otherNode.fireEvent(new TactilePaneEvent(TactilePaneEvent.AREA_LEFT, otherNode, thisNode));
                    }
                    if (proximityBounds != null && proximityBounds.intersects(otherBounds)) {
                        if (TactilePane.getNodesInProximity(thisNode).add(otherNode)) {
                            TactilePane.getNodesInProximity(otherNode).add(thisNode);

                            thisNode.fireEvent(new TactilePaneEvent(TactilePaneEvent.PROXIMITY_ENTERED, thisNode, otherNode));
                            otherNode.fireEvent(new TactilePaneEvent(TactilePaneEvent.PROXIMITY_ENTERED, otherNode, thisNode));
                        }
                        thisNode.fireEvent(new TactilePaneEvent(TactilePaneEvent.IN_PROXIMITY, thisNode, otherNode));
                    } else {
                        if (TactilePane.getNodesInProximity(thisNode).remove(otherNode)) {
                            TactilePane.getNodesInProximity(otherNode).remove(thisNode);

                            thisNode.fireEvent(new TactilePaneEvent(TactilePaneEvent.PROXIMITY_LEFT, thisNode, otherNode));
                            otherNode.fireEvent(new TactilePaneEvent(TactilePaneEvent.PROXIMITY_LEFT, otherNode, thisNode));
                        }
                    }
                }
            }
        }
    }
}
