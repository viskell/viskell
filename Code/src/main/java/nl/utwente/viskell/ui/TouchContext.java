package nl.utwente.viskell.ui;

import java.util.function.BiConsumer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

/**
 * Helper class used for event handling on the background pane of a container.
 */
public class TouchContext {

    /** The container this context handling events for. */
    private final BlockContainer container;

    /** the last mouse Position the pan action was handled for */
    private Point2D lastPanPosition;
    
    /** Boolean to indicate that a drag (pan) action has started, yet not finished. */
    private boolean panning;
    
    /** the action to be executed on a panning movement, may be null. */
    private BiConsumer<Double, Double> panningAction;
    
    private boolean willPanTouchArea;
    
    /** The line shown for the wire cutting mouse action, might be null if not valid. */
    private Line mouseCutLine;
    
    public TouchContext(BlockContainer container, boolean willPanTouchArea) {
        super();
        this.container = container;
        this.willPanTouchArea = willPanTouchArea;
        
        this.lastPanPosition = Point2D.ZERO;
        this.panning = false;
        this.panningAction = null;
        
        container.asNode().addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePress);
        container.asNode().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDrag);
        container.asNode().addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseRelease);
        container.asNode().addEventHandler(TouchEvent.TOUCH_PRESSED, this::handleTouchPress);
    }
    
    private void dropMouseCutLine() {
        if (this.mouseCutLine != null) {
            this.container.getToplevel().removeUpperTouchArea(this.mouseCutLine);
            this.mouseCutLine = null;
        }
    }
    
    private void handleMousePress(MouseEvent e) {
        if (!e.isSynthesized()) {
            this.lastPanPosition = new Point2D(e.getScreenX(), e.getScreenY());
            
            if (e.getButton() == MouseButton.PRIMARY && this.mouseCutLine == null) {
                Point2D pos = this.container.getToplevel().sceneToLocal(e.getSceneX(), e.getSceneY());
                this.mouseCutLine = new Line(pos.getX(), pos.getY(), pos.getX(), pos.getY());
                this.mouseCutLine.setStroke(Color.YELLOW);
                this.mouseCutLine.setStrokeWidth(3);
                this.mouseCutLine.setVisible(false);
                this.container.getToplevel().addUpperTouchArea(this.mouseCutLine);
            }
        }
        e.consume();
    }

    private void handleMouseDrag(MouseEvent e) {
        if (e.isSynthesized()) {
            e.consume();
            return;
        }
        
        Point2D currentPos = new Point2D(e.getScreenX(), e.getScreenY());
        if (!e.isPrimaryButtonDown()) {
            Point2D delta = currentPos.subtract(this.lastPanPosition);
            if (this.panning) {
                if (this.panningAction != null) {
                    this.panningAction.accept(delta.getX(), delta.getY());
                }
            } else {
                this.panning = (Math.abs(delta.getX()) +  Math.abs(delta.getY())) > 2;
            }
        } else if (this.mouseCutLine != null) {
            Point2D newPos = this.container.getToplevel().screenToLocal(currentPos);
            double lineDiffX = this.mouseCutLine.getStartX() - this.mouseCutLine.getEndX();
            double lineDiffY = this.mouseCutLine.getStartY() - this.mouseCutLine.getEndY();
            double lengthSQ = lineDiffX*lineDiffX + lineDiffY*lineDiffY;
            double distance = new Point2D(this.mouseCutLine.getStartX(), this.mouseCutLine.getStartY()).distance(newPos);
            if (distance*distance > lengthSQ) {
                this.mouseCutLine.setEndX(newPos.getX());
                this.mouseCutLine.setEndY(newPos.getY());
                if (distance > 300) {
                    this.dropMouseCutLine();
                } else if (distance > 75) {
                    this.mouseCutLine.setVisible(true);
                }
                
            } else if (distance < 10 && lengthSQ > 100*100) {
                double midX = (this.mouseCutLine.getStartX()+this.mouseCutLine.getEndX())/2;
                double midY = (this.mouseCutLine.getStartY()+this.mouseCutLine.getEndY())/2;
                Circle cutArea = new Circle(midX, midY, 40);
                this.container.getToplevel().addUpperTouchArea(cutArea);
                this.container.getToplevel().cutIntersectingConnections(cutArea);
                this.container.getToplevel().removeUpperTouchArea(cutArea);
                this.dropMouseCutLine();
            }
        }
        
        this.lastPanPosition = currentPos;
        e.consume();
    }
    
    private void handleMouseRelease(MouseEvent e) {
        if (e.isSynthesized()) {
            return;
        }
        
        if (e.getButton() != MouseButton.PRIMARY && !this.panning) {
            Point2D pos = this.container.getToplevel().sceneToLocal(this.container.asNode().localToScene(e.getX(), e.getY()));
            this.container.getToplevel().showFunctionMenuAt(pos.getX(), pos.getY(), true);
        }
        
        this.panning = false;
        this.dropMouseCutLine();
        e.consume();
    }
    
    private void handleTouchPress(TouchEvent e) {
        this.container.getToplevel().addLowerTouchArea(new TouchArea(e.getTouchPoint()));
        e.consume();
    }
    
    public void setPanningAction(BiConsumer<Double, Double> action) {
        this.panningAction = action;
    }
    
    
    /** A circular local area for handling multi finger touch actions. */
    private class TouchArea extends Circle {
        private final ToplevelPane toplevel;
        
        /** The ID of finger that spawned this touch area. */
        private int touchID;
        
        /** Whether this touch area has been dragged further than the drag threshold. */
        private boolean dragStarted;
        
        /** Whether this touch area has spawned a menu.  */
        private boolean menuCreated;
        
        /** Timed delay for the removal of this touch area. */
        private Timeline removeDelay;
        
        /** Timed delay for the creation of the function menu. */
        private Timeline menuDelay;
        
        /** The line shown for the wire cutting gesture, might be null if not valid. */
        private Line wireCutter;

        /**
         * @param touchPoint that is the center of new active touch area.
         */
        private TouchArea(TouchPoint touchPoint) {
            super();
            this.toplevel = TouchContext.this.container.getToplevel();
            this.touchID = touchPoint.getId();
            this.dragStarted = false;
            this.menuCreated = false;

            Point2D pos = this.toplevel.sceneToLocal(touchPoint.getSceneX(), touchPoint.getSceneY());
            this.setCenterX(pos.getX());
            this.setCenterY(pos.getY());
            this.setRadius(100);
            this.setFill(Color.TRANSPARENT);
            
            this.removeDelay = new Timeline(new KeyFrame(Duration.millis(250), this::remove));
            this.menuDelay = new Timeline(new KeyFrame(Duration.millis(200), this::finishMenu));
            
            this.wireCutter = new Line(pos.getX(), pos.getY(), pos.getX(), pos.getY());
            this.wireCutter.setStroke(Color.YELLOW);
            this.wireCutter.setStrokeWidth(3);
            this.wireCutter.setVisible(false);
            this.toplevel.addUpperTouchArea(this.wireCutter);
            
            touchPoint.grab(this);
            this.addEventHandler(TouchEvent.TOUCH_RELEASED, this::handleRelease);
            this.addEventHandler(TouchEvent.TOUCH_PRESSED, this::handlePress);
            this.addEventHandler(TouchEvent.TOUCH_MOVED, this::handleDrag);
        }
        
        private void remove(ActionEvent event) {
            this.toplevel.removeLowerTouchArea(this);
            this.removeCutter();
        }
       
        private void removeCutter(){
            if (this.wireCutter != null) {
                this.toplevel.removeUpperTouchArea(this.wireCutter);
                this.wireCutter = null;
            }
        }
        
        private void finishMenu(ActionEvent event) {
            this.toplevel.showFunctionMenuAt(this.getCenterX(), this.getCenterY(), false);
            this.toplevel.removeLowerTouchArea(this);
            this.menuCreated = true;
        }
        
        private void handlePress(TouchEvent event) {
            // this might have been a drag glitch, so halt release actions
            this.removeDelay.stop();
            if (event.getTouchPoints().stream().filter(tp -> tp.belongsTo(this)).count() == 2) {
                this.menuDelay.stop();
                this.removeCutter();
            }
            event.consume();
        }
        
        private void handleRelease(TouchEvent event) {
            long fingerCount = event.getTouchPoints().stream().filter(tp -> tp.belongsTo(this)).count();

            if (fingerCount == 1) {
                // trigger area removal timer
                this.removeDelay.play();
            } else if (this.dragStarted || this.menuCreated) {
                // avoid accidental creation of (more) menus
            } else if (fingerCount == 2) {
                // trigger menu creation timer
                this.menuDelay.play();
            }
            
            event.consume();
        }
        
        private void handleDrag(TouchEvent event) {
            TouchPoint touchPoint = event.getTouchPoint();
            
            if (event.getTouchPoint().getId() != this.touchID) {
                // we use only primary finger for drag movement
            } else if (event.getTouchPoints().stream().filter(tp -> tp.belongsTo(this)).count() < 2) {
                // not a multi finger drag
                this.updateCutter(this.toplevel.sceneToLocal(touchPoint.getSceneX(), touchPoint.getSceneY()));
            } else {
                double deltaX = touchPoint.getX() - this.getCenterX();
                double deltaY = touchPoint.getY() - this.getCenterY();
                
                if (Math.abs(deltaX) + Math.abs(deltaY) < 2) {
                    // ignore very small movements
                } else if ((deltaX*deltaX + deltaY*deltaY) > 10000) {
                    // FIXME: ignore too large movements
                } else if (this.dragStarted || (deltaX*deltaX + deltaY*deltaY) > 63) {
                    this.dragStarted = true;
                    if (TouchContext.this.panningAction != null) {
                        TouchContext.this.panningAction.accept(deltaX, deltaY);
                        if (!TouchContext.this.willPanTouchArea) {
                            this.setLayoutX(this.getLayoutX() + deltaX);
                            this.setLayoutY(this.getLayoutY() + deltaY);
                        }
                    }
                }
            }
            
            event.consume();
        }

        private void updateCutter(Point2D newPos) {
            if (this.wireCutter == null) {
                return;
            }
            
            double lineDiffX = this.wireCutter.getStartX() - this.wireCutter.getEndX();
            double lineDiffY = this.wireCutter.getStartY() - this.wireCutter.getEndY();
            double lengthSQ = lineDiffX*lineDiffX + lineDiffY*lineDiffY;
            double distance = new Point2D(this.wireCutter.getStartX(), this.wireCutter.getStartY()).distance(newPos);
            if (distance*distance > lengthSQ) {
                this.wireCutter.setEndX(newPos.getX());
                this.wireCutter.setEndY(newPos.getY());
                if (distance > 300) {
                    this.removeCutter();
                } else if (distance > 75) {
                    this.wireCutter.setVisible(true);
                }
                
            } else if (distance < 20 && lengthSQ > 100*100) {
                double midX = (this.wireCutter.getStartX()+this.wireCutter.getEndX())/2;
                double midY = (this.wireCutter.getStartY()+this.wireCutter.getEndY())/2;
                Circle cutArea = new Circle(midX, midY, 40);
                this.toplevel.addUpperTouchArea(cutArea);
                this.toplevel.cutIntersectingConnections(cutArea);
                this.toplevel.removeUpperTouchArea(cutArea);
                this.removeCutter();
            }
        }
    }
}
