package nl.utwente.viskell.ui.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
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
import javafx.util.Duration;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.ToplevelPane;
import nl.utwente.viskell.ui.WireMenu;

/**
 * A DrawWire represents the UI for a new incomplete connection is the process of being drawn. 
 * It is linked to a single Anchor as starting point, and with a second anchor it produces a new Connection.
 */
public class DrawWire extends CubicCurve implements ChangeListener<Transform>, ComponentLoader {

    /** The Anchor this wire is connected to */
    protected final ConnectionAnchor anchor;

    /** The Anchor this wire has been initiated from */
    private final ConnectionAnchor initAnchor;

    private TouchArea toucharea;
    
    private WireMenu menu;
    
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
        this.setFreePosition(initAnchor.getAttachmentPoint());
        anchor.localToSceneTransformProperty().addListener(x -> this.invalidateAnchorPosition());
        
        if (touchPoint != null) {
            this.toucharea = new TouchArea(touchPoint);
            pane.addTouchArea(this.toucharea);
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

    public ConnectionAnchor getAnchor() {
        return this.anchor;
    }
    
    private void showMenu(boolean byMouse) {
        if (this.menu == null) {
            this.menu = new WireMenu(this, byMouse);
            this.menu.relocate(this.getEndX() + 50 , this.getEndY() - 50);
            this.anchor.block.getToplevel().addMenu(this.menu);
        }
    }
    
    protected void handleMouseDrag(MouseEvent event) {
        if (this.menu == null) {
            Point2D localPos = this.anchor.getPane().sceneToLocal(event.getSceneX(), event.getSceneY());
            this.setFreePosition(localPos);
        }
        event.consume();
    }

    protected void handleMouseRelease(MouseEvent event) {
        if (this.menu != null) {
            // release has no effect if there is a menu
        } else if (event.getButton() == MouseButton.PRIMARY) {
            this.handleReleaseOn(event.getPickResult().getIntersectedNode());
        } else {
            this.showMenu(true);
        }
        event.consume();
    }

    private void handleReleaseOn(Node picked) {
        Node next = picked;
        ConnectionAnchor target = null;
        while (next != null) {
            if (next instanceof ConnectionAnchor.Target) {
                target = ((ConnectionAnchor.Target)next).getAssociatedAnchor();
                break;
            }
            next = next.getParent();
        }

        if (target != null) {
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
    public Connection buildConnectionTo(ConnectionAnchor target) {
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
        if (this.menu != null) {
            this.menu.close();
            this.menu = null;
        }
        
        if (this.toucharea != null) {
            this.toucharea.remove();
        }
        
        this.initAnchor.setWireInProgress(null);
        this.anchor.localToSceneTransformProperty().removeListener(this);
        this.anchor.getPane().removeWire(this);
    }

    @Override
    public void changed(ObservableValue<? extends Transform> observable, Transform oldValue, Transform newValue) {
        this.invalidateAnchorPosition();
    }

    /** Update the UI position of the anchor. */
    private void invalidateAnchorPosition() {
        Point2D point = this.anchor.getAttachmentPoint();
        if (this.anchor instanceof InputAnchor) {
            this.setEndX(point.getX());
            this.setEndY(point.getY());
        
        } else {
            this.setStartX(point.getX());
            this.setStartY(point.getY());
        }

        Connection.updateBezierControlPoints(this);
    }

    /**
     * Sets the free end coordinates for this wire.
     * @param point coordinates local to this wire's parent.
     */
    public void setFreePosition(Point2D point) {
        if (this.anchor instanceof InputAnchor) {
            this.setStartX(point.getX());
            this.setStartY(point.getY());
            
        } else {
            this.setEndX(point.getX());
            this.setEndY(point.getY());
        }
        
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
        
        /**
         * @param touchPoint that is the center of new active touch area.
         */
        private TouchArea(TouchPoint touchPoint) {
            super();
            this.setLayoutX(DrawWire.this.getEndX());
            this.setLayoutY(DrawWire.this.getEndY());
            this.touchID = touchPoint.getId();
            this.dragStarted = true;
            
            // a circle with hole is built from a path of round arcs with a very thick stroke
            ArcTo arc1 = new ArcTo(100, 100, 0, 100, 0, true, true);
            ArcTo arc2 = new ArcTo(100, 100, 0, -100, 0, true, true);
            this.getElements().addAll(new MoveTo(-100, 0), arc1, arc2, new ClosePath());
            this.setStrokeWidth(90);
            this.setStroke(Color.web("#0066FF"));
            this.setStrokeType(StrokeType.INSIDE);
            this.setOpacity(0);

            touchPoint.grab(this);
            this.addEventHandler(TouchEvent.TOUCH_RELEASED, this::handleRelease);
            this.addEventHandler(TouchEvent.TOUCH_PRESSED, this::handlePress);
            this.addEventHandler(TouchEvent.TOUCH_MOVED, this::handleDrag);
        }
        
        private void remove() {
            ToplevelPane pane = DrawWire.this.anchor.getPane();
            pane.removeTouchArea(this);
        }

        private void handlePress(TouchEvent event) {
            if (DrawWire.this.menu != null) {
                this.touchID = event.getTouchPoint().getId();
            }
            event.consume();
        }
        
        private void handleRelease(TouchEvent event) {
            long fingerCount = event.getTouchPoints().stream().filter(tp -> tp.belongsTo(this)).count();

            if (fingerCount == 1 && DrawWire.this.menu == null) {
                this.remove();
                Node picked = event.getTouchPoint().getPickResult().getIntersectedNode();
                DrawWire.this.handleReleaseOn(picked);
            } else if (DrawWire.this.menu != null) {
                // avoid accidental creation of (more) menus
            } else if (fingerCount == 2) {
                DrawWire.this.showMenu(false);
                this.dragStarted = false;
                // a delay to avoid the background picking up jitter from this event
                Timeline delay = new Timeline(new KeyFrame(Duration.millis(250), e -> {
                    this.setScaleX(0.25);
                    this.setScaleY(0.25);
                    this.setOpacity(0.4);
                }));
                delay.play();
            }
            
            event.consume();
        }
        
        private void handleDrag(TouchEvent event) {
            if (event.getTouchPoint().getId() != this.touchID) {
                // we use only primary finger for drag movement
            } else {
                double scaleFactor = this.getScaleX();
                double deltaX = event.getTouchPoint().getX() * scaleFactor;
                double deltaY = event.getTouchPoint().getY() * scaleFactor;
                
                if (Math.abs(deltaX) + Math.abs(deltaY) < 2) {
                    // ignore very small movements
                } else if ((deltaX*deltaX + deltaY*deltaY) > 10000) {
                    // FIXME: ignore too large movements
                } else if (this.dragStarted || (deltaX*deltaX + deltaY*deltaY) > 35) {
                    if (!this.dragStarted) {
                        this.handleDragStart();
                    }
 
                    double newX = this.getLayoutX() + deltaX;
                    double newY = this.getLayoutY() + deltaY;
                    this.setLayoutX(newX);
                    this.setLayoutY(newY);
                    DrawWire.this.setFreePosition(new Point2D(newX, newY));
                }
            }
            
            event.consume();
        }
        
        private void handleDragStart() {
            this.dragStarted = true;
            if (DrawWire.this.menu != null) {
                // resume dragging the wire
                DrawWire.this.menu.close();
                DrawWire.this.menu = null;
                this.setScaleX(1);
                this.setScaleY(1);
                this.setOpacity(0);
            }
        }
    }

}
