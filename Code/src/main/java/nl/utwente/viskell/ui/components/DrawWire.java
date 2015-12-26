package nl.utwente.viskell.ui.components;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Transform;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.ToplevelPane;

/**
 * A DrawWire represents the UI for a new incomplete connection is the process of being drawn. 
 * It is linked to a single Anchor as starting point, and with a second anchor it produces a new Connection.
 */
public class DrawWire extends CubicCurve implements ChangeListener<Transform>, ComponentLoader {

    /** The Anchor this wire is connected to */
    protected final ConnectionAnchor anchor;

    /** The Anchor this wire has been initiated from */
    private final ConnectionAnchor initAnchor;

    /**
     * @param anchor the connected side of this new wire.
     * @param initAnchor the anchor where this wire was initiated from.
     * @param touchPoint that initiated this wire, or null if it was by mouse. 
     */
    private DrawWire(ConnectionAnchor anchor, ConnectionAnchor initAnchor, TouchPoint touchPoint) {
        this.setMouseTransparent(true);
        this.anchor = anchor;
        this.initAnchor = initAnchor;

        ToplevelPane pane = anchor.getPane();
        pane.addWire(this);
        Point2D initPos = pane.sceneToLocal(initAnchor.localToScene(new Point2D(0, 0)));
        this.setFreePosition(initPos);
        anchor.localToSceneTransformProperty().addListener(x -> this.invalidateAnchorPosition());
        
        if (touchPoint != null) {
            pane.addTouchArea(new TouchArea(touchPoint));
        }
    }

    protected static DrawWire initiate(ConnectionAnchor anchor, TouchPoint touchPoint) {
        if (anchor instanceof InputAnchor && ((InputAnchor)anchor).hasConnection()) {
            // make room for a new connection by removing existing one
            Connection conn = ((InputAnchor)anchor).getConnection().get();
            conn.remove();
            // keep the other end of old connection to initiate the new one
            return new DrawWire(conn.getStartAnchor(), anchor, touchPoint);
        } else {
            return new DrawWire(anchor, anchor, touchPoint);
        }
    }

    protected void handleMouseDrag(MouseEvent event) {
        Point2D localPos = this.anchor.getPane().sceneToLocal(event.getSceneX(), event.getSceneY());
        this.setFreePosition(localPos);
        event.consume();
    }

    protected void handleMouseRelease(MouseEvent event) {
        this.handleReleaseOn(event.getPickResult().getIntersectedNode());
        event.consume();
    }

    private void handleReleaseOn(Node picked) {
        if (picked.getParent() instanceof ConnectionAnchor) {
            ConnectionAnchor target = (ConnectionAnchor)picked.getParent();
            Connection connection = this.buildConnectionTo(target);
            if (connection != null) {
                connection.getStartAnchor().initiateConnectionChanges();
            }
        }
        // drop the wire, even if connection failed
        this.remove();
    }

    /**
     * Constructs a new Connection from this partial wire and another anchor
     * @param target the Anchor to which the other end of this should be connection to.
     * @return the newly build Connection or null if it's not possible
     */
    private Connection buildConnectionTo(ConnectionAnchor target) {
        InputAnchor sink;
        OutputAnchor source;
        if (this.anchor instanceof InputAnchor) {
            if (target instanceof InputAnchor) {
                return null;
            }
            sink = (InputAnchor)this.anchor;
            source = (OutputAnchor)target;
        } else {
            if (target instanceof OutputAnchor) {
                return null;
            }
            sink = (InputAnchor)target;
            source = (OutputAnchor)this.anchor;

            if (sink.hasConnection()) {
                sink.removeConnections(); // push out the existing connection
            }
        }

        return new Connection(source, sink);
    }

    /** Removes this wire from its pane, and its listener. */
    public final void remove() {
        this.initAnchor.wireInProgress = null;
        this.anchor.localToSceneTransformProperty().removeListener(this);
        this.anchor.getPane().removeWire(this);
    }

    @Override
    public void changed(ObservableValue<? extends Transform> observable, Transform oldValue, Transform newValue) {
        this.invalidateAnchorPosition();
    }

    /** Update the UI position of the anchor. */
    private void invalidateAnchorPosition() {
        Point2D center = (this.anchor instanceof InputAnchor) ? new Point2D(0, -4) : new Point2D(0, 4);
        Point2D point = this.anchor.getPane().sceneToLocal(this.anchor.localToScene(center));
        this.setStartX(point.getX());
        this.setStartY(point.getY());
        this.updateBezierControlPoints();
    }

    /**
     * Sets the free end coordinates for this wire.
     * @param point coordinates local to this wire's parent.
     */
    public void setFreePosition(Point2D point) {
        this.setEndX(point.getX());
        this.setEndY(point.getY());
        this.invalidateAnchorPosition();

        ToplevelPane pane = this.anchor.block.getToplevel();
        Point2D scenePoint = pane.localToScene(point, false);
        BlockContainer anchorContainer = this.anchor.getContainer();
        boolean scopeOK = true;

        if (this.anchor instanceof OutputAnchor) {
            scopeOK = anchorContainer.getBoundsInScene().contains(scenePoint);
        } else if (this.anchor instanceof InputAnchor) {
            scopeOK = pane.getAllBlockContainers().
                    filter(con -> con.getBoundsInScene().contains(scenePoint)).
                    allMatch(con -> anchorContainer.isContainedWithin(con));
        }

        if (scopeOK) {
            this.getStrokeDashArray().clear();
        } else if (this.getStrokeDashArray().isEmpty()) {
            this.getStrokeDashArray().addAll(15.0, 15.0);
        }

    }

    /** A circular area at the open end of the draw wire for handling multi finger touch actions.
     *  This area has as a workaround a hole in the middle to be able to pick the thing behind it on release.
     */
    private class TouchArea extends Path {
        /** The ID of finger that spawned this touch area. */
        private int touchID;
        
        /** Whether this touch area has been dragged further than the drag threshold. */
        private boolean dragStarted;
        
        /** Whether this touch area has spawned a menu.  */
        private boolean menuCreated;
        
        /**
         * @param touchPoint that is the center of new active touch area.
         */
        private TouchArea(TouchPoint touchPoint) {
            super();
            this.setLayoutX(DrawWire.this.getEndX());
            this.setLayoutY(DrawWire.this.getEndY());
            this.touchID = touchPoint.getId();
            this.dragStarted = true;
            this.menuCreated = false;
            
            // a circle with hole is built from a path of round arcs with a very thick stroke
            ArcTo arc1 = new ArcTo(100, 100, 0, 100, 0, true, true);
            ArcTo arc2 = new ArcTo(100, 100, 0, -100, 0, true, true);
            this.getElements().addAll(new MoveTo(-100, 0), arc1, arc2, new ClosePath());
            this.setStrokeWidth(80);
            this.setStroke(Color.TRANSPARENT);
            this.setStrokeType(StrokeType.INSIDE);

            touchPoint.grab(this);
            this.addEventHandler(TouchEvent.TOUCH_RELEASED, this::handleRelease);
            this.addEventHandler(TouchEvent.TOUCH_PRESSED, this::handlePress);
            this.addEventHandler(TouchEvent.TOUCH_MOVED, this::handleDrag);
        }
        
        private void finishMenu(ActionEvent event) {
            // TODO
            this.menuCreated = true;
        }
        
        private void handlePress(TouchEvent event) {
            event.consume();
        }
        
        private void handleRelease(TouchEvent event) {
            long fingerCount = event.getTouchPoints().stream().filter(tp -> tp.belongsTo(this)).count();

            if (fingerCount == 1) {
                ToplevelPane pane = DrawWire.this.anchor.getPane();
                pane.removeTouchArea(this);
                Node picked = event.getTouchPoint().getPickResult().getIntersectedNode();
                DrawWire.this.handleReleaseOn(picked);
            } else if (this.dragStarted || this.menuCreated) {
                // avoid accidental creation of (more) menus
            } else if (fingerCount == 2) {
                // TODO wire menu
            }
            
            event.consume();
        }
        
        private void handleDrag(TouchEvent event) {
            if (event.getTouchPoint().getId() != this.touchID) {
                // we use only primary finger for drag movement
            } else {
                double deltaX = event.getTouchPoint().getX();
                double deltaY = event.getTouchPoint().getY();
                
                if (Math.abs(deltaX) + Math.abs(deltaY) < 2) {
                    // ignore very small movements
                } else if ((deltaX*deltaX + deltaY*deltaY) > 10000) {
                    // FIXME: ignore too large movements
                } else if (this.dragStarted || (deltaX*deltaX + deltaY*deltaY) > 24) {
                    this.dragStarted = true;
                    double newX = this.getLayoutX() + deltaX;
                    double newY = this.getLayoutY() + deltaY;
                    this.setLayoutX(newX);
                    this.setLayoutY(newY);
                    DrawWire.this.setFreePosition(new Point2D(newX, newY));
                }
            }
            
            event.consume();
        }
        
    }

    /** Updates the Bezier offset (curviness) according to the current start and end positions. */
    private void updateBezierControlPoints() {
        double BEZIER_CONTROL_OFFSET = Connection.BEZIER_CONTROL_OFFSET;
        double distY = Math.abs(this.getEndY() - this.getStartY());
        double yOffset = BEZIER_CONTROL_OFFSET;
        if (distY > BEZIER_CONTROL_OFFSET) {
            yOffset = Math.cbrt(distY / BEZIER_CONTROL_OFFSET) * BEZIER_CONTROL_OFFSET;
        }

        this.setControlX1(this.getStartX());
        this.setControlX2(this.getEndX());
        if (this.anchor instanceof OutputAnchor) {
            this.setControlY1(this.getStartY() + yOffset);
        } else {
            this.setControlY1(this.getStartY() - yOffset);
        }
        if (this.getStartY() > this.getEndY()) {
            this.setControlY2(this.getEndY()   + yOffset);
        } else {
            this.setControlY2(this.getEndY()   - yOffset);
        }
    }

}
