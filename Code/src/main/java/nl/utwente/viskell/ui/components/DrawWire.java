package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import nl.utwente.viskell.haskell.env.DataTypeInfo;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.haskell.type.*;
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

    private final TouchArea toucharea;
    
    private WireMenu menu;
    
    /**
     * @param anchor the connected side of this new wire.
     * @param startingPoint the position where this wire was initiated from.
     * @param touchPoint that initiated this wire, or null if it was by mouse. 
     */
    private DrawWire(ConnectionAnchor anchor, Point2D startingPoint, TouchPoint touchPoint) {
        this.setMouseTransparent(true);
        this.anchor = anchor;
        this.anchor.setWireInProgress(this);

        ToplevelPane pane = anchor.getPane();
        pane.addWire(this);
        this.setFreePosition(startingPoint);
        anchor.localToSceneTransformProperty().addListener(this);
        
        this.toucharea = new TouchArea(touchPoint);
        pane.addUpperTouchArea(this.toucharea);
    }

    protected static DrawWire initiate(ConnectionAnchor anchor, TouchPoint touchPoint) {
        if (anchor instanceof InputAnchor && anchor.hasConnection()) {
            Connection conn = ((InputAnchor)anchor).getConnection().get();
            OutputAnchor startAnchor = conn.getStartAnchor();
            if (startAnchor.getWireInProgress() == null) {
                // make room for a new connection by removing existing one
                conn.remove();
                // keep the other end of old connection to initiate the new one
                return new DrawWire(startAnchor, anchor.getAttachmentPoint(), touchPoint);
            } else {
                return null;
            }
        } else if (anchor instanceof OutputAnchor && anchor.hasConnection() && ((OutputAnchor)anchor).connections.get(0).hasTypeError()) {
            Connection conn = ((OutputAnchor)anchor).connections.get(0);
            InputAnchor endAnchor = conn.getEndAnchor();
            if (endAnchor.getWireInProgress() == null) {
                // trying to solve the type error by changing the connection
                conn.remove();
                return new DrawWire(endAnchor, anchor.getAttachmentPoint(), touchPoint);
            }
        }

        return new DrawWire(anchor, anchor.getAttachmentPoint(), touchPoint);
    }

    public ConnectionAnchor getAnchor() {
        return this.anchor;
    }
    
    private void showMenu(boolean byMouse) {
        if (this.menu == null) {
            this.menu = new WireMenu(this, byMouse);
            this.menu.relocate(this.toucharea.getLayoutX() + 50 , this.toucharea.getLayoutY() - 50);
            this.anchor.block.getToplevel().addMenu(this.menu);
        }
    }
    
    protected void handleMouseDrag(MouseEvent event) {
        if (this.menu == null && !event.isSynthesized()) {
            Point2D localPos = this.anchor.getPane().sceneToLocal(event.getSceneX(), event.getSceneY());
            this.toucharea.dragTo(localPos.getX(), localPos.getY(), event.getPickResult().getIntersectedNode());
        }
        event.consume();
    }

    protected void handleMouseRelease(MouseEvent event) {
        this.toucharea.handleMouseRelease(event);
    }

    /** @return the ConnectionAnchor related to the picked Node, or null of none. */
    private static ConnectionAnchor findPickedAnchor(Node picked) {
        Node next = picked;
        while (next != null) {
            if (next instanceof ConnectionAnchor.Target) {
                return ((ConnectionAnchor.Target)next).getAssociatedAnchor();
            }
            next = next.getParent();
        }
        
        return null;
    }
    
    private void handleReleaseOn(Node picked) {
        ConnectionAnchor target = findPickedAnchor(picked);
        if (target == this.anchor) {
            this.toucharea.handleReleaseOnSelf();
        } else if (target != null && target.getWireInProgress() == null) {
            Connection connection = this.buildConnectionTo(target);
            if (connection != null) {
                connection.getStartAnchor().initiateConnectionChanges();
                this.remove();
            } else {
                this.toucharea.handleReleaseOnNothing();
            }
        } else if (Connection.lengthSquared(this) < 900) {
            this.toucharea.handleReleaseOnSelf();
        } else {
            this.toucharea.handleReleaseOnNothing();
        }
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

        if (sink.block == source.block && !(sink instanceof ResultAnchor && source instanceof BinderAnchor)) {
            // self recursive wires are not allowed
            return null;
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
        
        this.anchor.setWireInProgress(null);
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
            scopeOK = anchorContainer.containmentBoundsInScene().contains(scenePoint);
        } else if (this.anchor instanceof InputAnchor) {
            scopeOK = pane.getAllBlockContainers().
                    filter(con -> con.containmentBoundsInScene().contains(scenePoint)).
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
        
        /** Timed animation for toucharea and drawwire self removal */
        private final Timeline disapperance;
        
        /** List of nearby anchors that have visually reacted to this wire. */
        private List<ConnectionAnchor> nearbyAnchors;
        
        private Point2D lastNearbyUpdate;
        
        /**
         * @param touchPoint that is the center of new active touch area, or null if the mouse
         */
        private TouchArea(TouchPoint touchPoint) {
            super();
            this.setLayoutX(DrawWire.this.getEndX());
            this.setLayoutY(DrawWire.this.getEndY());
            
            this.touchID = touchPoint == null ? -1 : touchPoint.getId();
            this.dragStarted = true;
            this.nearbyAnchors = new ArrayList<>();
            this.lastNearbyUpdate = Point2D.ZERO;
            
            this.disapperance = new Timeline(new KeyFrame(Duration.millis(2000),
                    e -> DrawWire.this.remove(),
                    new KeyValue(this.opacityProperty(), 0.3),
                    new KeyValue(DrawWire.this.opacityProperty(), 0.2)));
            
            // a circle with hole is built from a path of round arcs with a very thick stroke
            ArcTo arc1 = new ArcTo(100, 100, 0, 100, 0, true, true);
            ArcTo arc2 = new ArcTo(100, 100, 0, -100, 0, true, true);
            this.getElements().addAll(new MoveTo(-100, 0), arc1, arc2, new ClosePath());
            this.setStroke(Color.web("#0066FF"));
            this.setStrokeType(StrokeType.INSIDE);
            this.setStrokeWidth(90);
            this.setStroke(Color.web("#0066FF"));
            this.setStrokeType(StrokeType.INSIDE);
            this.setOpacity(0);

            if (touchPoint != null) { 
                touchPoint.grab(this);
            }
            
            this.addEventHandler(TouchEvent.TOUCH_PRESSED, this::handleTouchPress);
            this.addEventHandler(TouchEvent.TOUCH_MOVED, this::handleTouchDrag);
            this.addEventHandler(TouchEvent.TOUCH_RELEASED, this::handleTouchRelease);
            this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePress);
            this.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDrag);
            this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseRelease);
        }
        
        private void handleReleaseOnNothing() {
            this.makeVisible();
            disapperance.playFromStart();
        }

        private void handleReleaseOnSelf() {
            this.makeVisible();
            disapperance.playFrom(disapperance.getTotalDuration().multiply(0.9));
        }
        
        private void makeVisible() {
            this.clearWireReactions();
            this.setScaleX(0.25);
            this.setScaleY(0.25);
            this.setOpacity(0.6);
            this.setStrokeWidth(99);
            DrawWire.this.setOpacity(1);
            // avoid clicking the hole with a non moving mouse
            this.setLayoutY(this.getLayoutY() + (DrawWire.this.anchor instanceof InputAnchor ? -2 : 2));   
        }
        
        private void clearWireReactions() {
            for (ConnectionAnchor anchor : this.nearbyAnchors) {
                anchor.setNearbyWireReaction(0);
            }
        }
        
        private void remove() {
            this.clearWireReactions();
            ToplevelPane pane = DrawWire.this.anchor.getPane();
            pane.removeUpperTouchArea(this);
        }

        private void handleTouchPress(TouchEvent event) {
            if (!this.dragStarted) {
                this.touchID = event.getTouchPoint().getId();
                this.disapperance.stop();
                this.makeVisible();
            }
            event.consume();
        }
        
        private void handleMousePress(MouseEvent event) {
            if (event.isSynthesized()) {
             // don't react on synthesized events
            } else if (event.getButton() == MouseButton.PRIMARY) {
                this.touchID = -1;
                this.handleDragStart();
                this.disapperance.stop();
                DrawWire.this.setOpacity(1);
            } else {
                DrawWire.this.remove();
            }
            event.consume();
        }
        
        private void handleTouchRelease(TouchEvent event) {
            this.dragStarted = false;
            long fingerCount = event.getTouchPoints().stream().filter(tp -> tp.belongsTo(this)).count();

            if (fingerCount == 1 && DrawWire.this.menu == null) {
                Node picked = event.getTouchPoint().getPickResult().getIntersectedNode();
                DrawWire.this.handleReleaseOn(picked);
            } else if (DrawWire.this.menu != null || this.touchID < 0) {
                // avoid accidental creation of (more) menus
            } else if (fingerCount == 2) {
                DrawWire.this.showMenu(false);
                // a delay to avoid the background picking up jitter from this event
                Timeline delay = new Timeline(new KeyFrame(Duration.millis(250), e -> this.makeVisible()));
                delay.play();
            }
            
            event.consume();
        }
        
        private void handleMouseRelease(MouseEvent event) {
            if (event.isSynthesized()) {
                // don't react on synthesized events 
            } else if (DrawWire.this.menu != null) {
                // release has no effect if there is a menu
            } else if (event.getButton() == MouseButton.PRIMARY) {
                DrawWire.this.handleReleaseOn(event.getPickResult().getIntersectedNode());
                this.dragStarted = false;
            } else {
                DrawWire.this.showMenu(true);
                this.dragStarted = false;
                this.makeVisible();
            }
            event.consume();
        }
        
        private void handleTouchDrag(TouchEvent event) {
            double scaleFactor = this.getScaleX();
            double deltaX = event.getTouchPoint().getX() * scaleFactor;
            double deltaY = event.getTouchPoint().getY() * scaleFactor;

            if (event.getTouchPoint().getId() != this.touchID) {
                // we use only primary finger for drag movement
                if (this.dragStarted && Math.abs(deltaX) > 175) {
                    this.horizontalSplittingAction(event);
                } else if (this.dragStarted && Math.abs(deltaY) > 150){
                    this.verticalSplittingAction(event);
                }
            } else {
                
                if ((deltaX*deltaX + deltaY*deltaY) > 10000) {
                    // FIXME: ignore too large movements
                } else if (this.dragStarted || (deltaX*deltaX + deltaY*deltaY) > 63) {
                    if (!this.dragStarted) {
                        this.handleDragStart();
                    }
 
                    double newX = this.getLayoutX() + deltaX;
                    double newY = this.getLayoutY() + deltaY;
                    this.dragTo(newX, newY, event.getTouchPoint().getPickResult().getIntersectedNode());
                }
            }
            
            event.consume();
        }

        private void handleMouseDrag(MouseEvent event) {
            if (DrawWire.this.menu == null && !event.isSynthesized()) {
                double scaleFactor = this.getScaleX();
                double newX = this.getLayoutX() + event.getX() * scaleFactor;
                double newY = this.getLayoutY() + event.getY() * scaleFactor;
                this.dragTo(newX, newY, event.getPickResult().getIntersectedNode());
            }
            event.consume();
        }
        
        private void dragTo(double newX, double newY) {
            this.dragTo(newX, newY, null);
        }
        
        private void dragTo(double newX, double newY, Node picked) {
            this.setLayoutX(newX);
            this.setLayoutY(newY);
            Point2D newPos = new Point2D(newX, newY);
            DrawWire.this.setFreePosition(newPos);

            // threshold to avoid doing a quite expensive computation too often
            if (this.lastNearbyUpdate.distance(newPos) > 10) {
                this.lastNearbyUpdate = newPos;
                List<ConnectionAnchor> targetAnchors = anchor.block.getToplevel().allNearbyFreeAnchors(newPos, 166);
                List<ConnectionAnchor> newNearby = new ArrayList<>();

                // trial unification on all nearby opposite free anchor so see if they could fit
                if (DrawWire.this.anchor instanceof InputAnchor) {
                    InputAnchor anchor = (InputAnchor)DrawWire.this.anchor;
                    ConnectionAnchor releaseAnchor = DrawWire.findPickedAnchor(picked);
                    if (releaseAnchor == anchor) {
                        releaseAnchor = null;
                    }
                    
                    for (ConnectionAnchor target : targetAnchors) {
                        if (target instanceof OutputAnchor) {
                            target.setNearbyWireReaction(determineWireReaction((OutputAnchor)target, anchor, releaseAnchor));
                            newNearby.add(target);
                        }
                    }
                } else {
                    OutputAnchor anchor = (OutputAnchor)DrawWire.this.anchor;
                    ConnectionAnchor releaseAnchor = DrawWire.findPickedAnchor(picked);
                    if (releaseAnchor == anchor) {
                        releaseAnchor = null;
                    }

                    for (ConnectionAnchor target : targetAnchors) {
                        if (target instanceof InputAnchor) {
                            newNearby.add(target);
                            target.setNearbyWireReaction(determineWireReaction(anchor, (InputAnchor)target, releaseAnchor));
                        }
                    }
                }
                
                // reset all anchors that are not nearby anymore
                for (ConnectionAnchor oldNear : this.nearbyAnchors) {
                    if (! newNearby.contains(oldNear)) {
                        oldNear.setNearbyWireReaction(0);
                    }
                }
                
                this.nearbyAnchors = newNearby;
            }
        }
        
        private int determineWireReaction(OutputAnchor source, InputAnchor sink, ConnectionAnchor releaseAnchor) {
            if (sink.block == source.block && !(sink instanceof ResultAnchor  && source instanceof BinderAnchor)) {
                return 0;
            }
            
            try {
                TypeChecker.unify("wire reaction", source.getType(Optional.empty()).getFresh(), sink.getType().getFresh());
                if (source == releaseAnchor || sink == releaseAnchor) {
                    return 3;
                } else {
                    return 1;
                }
            } catch (HaskellTypeError e) {
                return -1;
            }
        }
        
        private void handleDragStart() {
            this.dragStarted = true;
            if (DrawWire.this.menu != null) {
                // resume dragging the wire
                DrawWire.this.menu.close();
                DrawWire.this.menu = null;
            }
            
            this.setScaleX(1);
            this.setScaleY(1);
            this.setOpacity(0);
            this.setStrokeWidth(90);
        }

        private void horizontalSplittingAction(TouchEvent event) {
            ToplevelPane toplevel = DrawWire.this.anchor.block.getToplevel();
            TouchPoint tpA = event.getTouchPoint();
            Point2D posA = toplevel.sceneToLocal(new Point2D(tpA.getSceneX(), tpA.getSceneY()));
            List<TouchPoint> tpis = event.getTouchPoints().stream().filter(tp -> tp.getId() == this.touchID).collect(Collectors.toList());
            if (tpis.isEmpty()) {
                return; // something is wrong with primary touchpoint, give up
            }
            
            TouchPoint tpB = tpis.get(0);
            Point2D posB = new Point2D(this.getLayoutX(), this.getLayoutY());
            this.dragStarted = false;
            this.touchID = -1;

            int tupleArity = 2;
            List<DataTypeInfo.Constructor> constrs = new ArrayList<>();
            Type type = DrawWire.this.anchor.getFreshType().getConcrete();
            if (type instanceof TypeApp) {
                List<Type> ts = ((TypeApp)type).asFlattenedAppChain();
                if (ts.get(0) instanceof TupleTypeCon) {
                    tupleArity = ts.size()-1;
                } else if (ts.get(0) instanceof TypeCon) {
                    HaskellCatalog catalog = toplevel.getGhciSession().getCatalog();
                    DataTypeInfo datatype = catalog.getDataType(((TypeCon)ts.get(0)).getName());
                    if (datatype != null && !datatype.isBuiltin()) {
                        constrs = datatype.getConstructors();
                    }
                }
            } else if (type instanceof TypeCon) {
                HaskellCatalog catalog = toplevel.getGhciSession().getCatalog();
                DataTypeInfo datatype = catalog.getDataType(((TypeCon)type).getName());
                if (datatype != null && !datatype.isBuiltin()) {
                    constrs = datatype.getConstructors();
                }
            }
            
            if (constrs.size() >= 2 && constrs.size() <= 5) {
                ChoiceBlock choice = new ChoiceBlock(toplevel);
                for (int i = 2; i < constrs.size(); i++) {
                    choice.addLane();
                }
                toplevel.addBlock(choice);
                double offsetX = tpA.getX() < 0 ? -300 : -150;

                if (DrawWire.this.anchor instanceof OutputAnchor) {
                    double posX = DrawWire.this.getEndX() + offsetX;
                    double posY = DrawWire.this.getEndY()-100;
                    choice.relocate(posX, posY);
                    choice.addExtraInput();
                    InputAnchor input = choice.getAllInputs().get(0);
                    Connection connection = DrawWire.this.buildConnectionTo(input);
                    if (connection != null) {
                        connection.getStartAnchor().initiateConnectionChanges();
                    }
                    
                    List<Block> decons = new ArrayList<>();
                    List<Lane> lanes = choice.getLanes();
                    for (int i = 0; i < constrs.size(); i++) {
                        Lane lane = lanes.get(i);
                        MatchBlock mblock = new MatchBlock(toplevel, toplevel.getEnvInstance().lookupFun(constrs.get(i).getName()));
                        decons.add(mblock);
                        toplevel.addBlock(mblock);
                        OutputAnchor arg = (OutputAnchor)lane.getAllAnchors().get(0);
                        mblock.relocate(posX+125 + (i*250), posY + 75);
                        DrawWire tempWire = DrawWire.initiate(arg, null);
                        tempWire.buildConnectionTo(mblock.input);
                        tempWire.remove();
                    }
                
                    Platform.runLater(() -> {
                        for (Block block : decons) {
                            block.refreshContainer();
                            block.initiateConnectionChanges();
                        }
                    });

                    DrawWire.this.remove();

                } else {
                    double posX = DrawWire.this.getEndX() + offsetX;
                    double posY = DrawWire.this.getEndY() - 450;
                    choice.relocate(posX, posY);
                    OutputAnchor output = choice.getAllOutputs().get(0);
                    Connection connection = DrawWire.this.buildConnectionTo(output);
                    if (connection != null) {
                        connection.getStartAnchor().initiateConnectionChanges();
                    }
                    
                    List<Block> conses = new ArrayList<>();
                    List<Lane> lanes = choice.getLanes();
                    for (int i = 0; i < constrs.size(); i++) {
                        Lane lane = lanes.get(i);
                        FunApplyBlock cblock = new FunApplyBlock(toplevel, new LibraryFunUse(toplevel.getEnvInstance().lookupFun(constrs.get(i).getName())));
                        conses.add(cblock);
                        toplevel.addBlock(cblock);
                        cblock.initiateConnectionChanges();
                        InputAnchor res = (InputAnchor)lane.getAllAnchors().get(0);
                        cblock.relocate(posX+125 + (i*250), posY + 250);
                        DrawWire tempWire = DrawWire.initiate(res, null);
                        tempWire.buildConnectionTo(cblock.getAllOutputs().get(0));
                        tempWire.remove();
                    }
                
                    Platform.runLater(() -> {
                        for (Block block : conses) {
                            block.refreshContainer();
                            block.initiateConnectionChanges();
                        }
                    });

                    DrawWire.this.remove();
     
                }


            } else {
            
                if (DrawWire.this.anchor instanceof OutputAnchor) {
                    Block block = new SplitterBlock(toplevel, tupleArity);
                    toplevel.addBlock(block);
                    double offsetX = tpA.getX() < 0 ? -75 : 75;
                    block.relocate(DrawWire.this.getEndX() + offsetX, DrawWire.this.getEndY()-100);
                    block.refreshContainer();
                    block.initiateConnectionChanges();

                    InputAnchor input = block.getAllInputs().get(0);
                    Connection connection = DrawWire.this.buildConnectionTo(input);
                    if (connection != null) {
                        connection.getStartAnchor().initiateConnectionChanges();
                    }

                    if (tpA.getX() < 0) {
                        DrawWire wireA = DrawWire.initiate(block.getAllOutputs().get(0), tpA);
                        wireA.toucharea.dragTo(posA.getX(), posA.getY());
                        DrawWire wireB = DrawWire.initiate(block.getAllOutputs().get(1), tpB);
                        wireB.toucharea.dragTo(posB.getX(), posB.getY());
                    } else {
                        DrawWire wireA = DrawWire.initiate(block.getAllOutputs().get(1), tpA);
                        wireA.toucharea.dragTo(posA.getX(), posA.getY());
                        DrawWire wireB = DrawWire.initiate(block.getAllOutputs().get(0), tpB);
                        wireB.toucharea.dragTo(posB.getX(), posB.getY());
                    }
                    DrawWire.this.remove();

                } else {
                    Block block = new JoinerBlock(toplevel, tupleArity);
                    toplevel.addBlock(block);
                    double offsetX = tpA.getX() < 0 ? -75 : 75;
                    block.relocate(DrawWire.this.getStartX() + offsetX, DrawWire.this.getStartY()+100);
                    block.refreshContainer();
                    block.initiateConnectionChanges();

                    OutputAnchor input = block.getAllOutputs().get(0);
                    Connection connection = DrawWire.this.buildConnectionTo(input);
                    if (connection != null) {
                        connection.getStartAnchor().initiateConnectionChanges();
                    }

                    if (tpA.getX() < 0) {
                        DrawWire wireA = DrawWire.initiate(block.getAllInputs().get(0), tpA);
                        wireA.toucharea.dragTo(posA.getX(), posA.getY());
                        DrawWire wireB = DrawWire.initiate(block.getAllInputs().get(1), tpB);
                        wireB.toucharea.dragTo(posB.getX(), posB.getY());
                    } else {
                        DrawWire wireA = DrawWire.initiate(block.getAllInputs().get(1), tpA);
                        wireA.toucharea.dragTo(posA.getX(), posA.getY());
                        DrawWire wireB = DrawWire.initiate(block.getAllInputs().get(0), tpB);
                        wireB.toucharea.dragTo(posB.getX(), posB.getY());
                    }
                    DrawWire.this.remove();
                }
            }
        }

        private void verticalSplittingAction(TouchEvent event) {
            ToplevelPane toplevel = DrawWire.this.anchor.block.getToplevel();
            TouchPoint tpA = event.getTouchPoint();
            Point2D posA = toplevel.sceneToLocal(new Point2D(tpA.getSceneX(), tpA.getSceneY()));
            List<TouchPoint> tpis = event.getTouchPoints().stream().filter(tp -> tp.getId() == this.touchID).collect(Collectors.toList());
            if (tpis.isEmpty()) {
                return; // something is wrong with primary touchpoint, give up
            }
            
            TouchPoint tpB = tpis.get(0);
            Point2D posB = new Point2D(this.getLayoutX(), this.getLayoutY());
            this.dragStarted = false;
            this.touchID = -1;

            Type type = DrawWire.this.anchor.getFreshType();
            int arity = type.countArguments();
            if (arity < 1) {
                return; // we only we with functions for now
            }
            
            if (DrawWire.this.anchor instanceof OutputAnchor) {
                Block block = new FunApplyBlock(toplevel, new ApplyAnchor(arity));
                toplevel.addBlock(block);
                double offsetY = tpA.getY() < 0 ? -100 : 50;
                block.relocate(DrawWire.this.getEndX() - 40, DrawWire.this.getEndY()+offsetY);
                block.refreshContainer();
                block.initiateConnectionChanges();

                InputAnchor input = block.getAllInputs().get(0);
                Connection connection = DrawWire.this.buildConnectionTo(input);
                if (connection != null) {
                    connection.getStartAnchor().initiateConnectionChanges();
                }
                
                if (tpA.getY() < 0) {
                    DrawWire wireA = DrawWire.initiate(block.getAllInputs().get(1), tpA);
                    wireA.toucharea.dragTo(posA.getX(), posA.getY());
                    DrawWire wireB = DrawWire.initiate(block.getAllOutputs().get(0), tpB);
                    wireB.toucharea.dragTo(posB.getX(), posB.getY());
                } else {
                    DrawWire wireA = DrawWire.initiate(block.getAllOutputs().get(0), tpA);
                    wireA.toucharea.dragTo(posA.getX(), posA.getY());
                    DrawWire wireB = DrawWire.initiate(block.getAllInputs().get(1), tpB);
                    wireB.toucharea.dragTo(posB.getX(), posB.getY());
                }
                DrawWire.this.remove();
                
            } else {
                LambdaBlock block = new LambdaBlock(toplevel, arity);
                toplevel.addBlock(block);
                double offsetY = tpA.getY() < 0 ? -240 : -100;
                block.relocate(DrawWire.this.getStartX()-150, DrawWire.this.getStartY() + offsetY);
                block.initiateConnectionChanges();

                OutputAnchor input = block.getAllOutputs().get(0);
                Connection connection = DrawWire.this.buildConnectionTo(input);
                if (connection != null) {
                    connection.getStartAnchor().initiateConnectionChanges();
                }
                LambdaContainer body = block.getBody(); 
                
                if (tpA.getY() < 0) {
                    DrawWire wireA = DrawWire.initiate(body.getAllAnchors().get(0), tpA);
                    wireA.toucharea.dragTo(posA.getX(), posA.getY());
                    DrawWire wireB = DrawWire.initiate(body.getAllAnchors().get(arity), tpB);
                    wireB.toucharea.dragTo(posB.getX(), posB.getY());
                } else {
                    DrawWire wireA = DrawWire.initiate(body.getAllAnchors().get(arity), tpA);
                    wireA.toucharea.dragTo(posA.getX(), posA.getY());
                    DrawWire wireB = DrawWire.initiate(body.getAllAnchors().get(0), tpB);
                    wireB.toucharea.dragTo(posB.getX(), posB.getY());
                }
                DrawWire.this.remove();
            }
            
        }

    }


}
