package nl.utwente.viskell.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.ui.components.Block;
import nl.utwente.viskell.ui.components.Connection;
import nl.utwente.viskell.ui.components.DrawWire;
import nl.utwente.viskell.ui.components.WrappedContainer;

/**
 * The core Pane that also keeps state for the user interface.
 */
public class CustomUIPane extends Region {
    /** bottom pane layer intended for block container such as lambda's */
    private final Pane bottomLayer;

    /** middle pane layer for ordinary blocks */
    private final Pane blockLayer;

    /** higher pane layer for connections wires */
    private final Pane wireLayer;

    /** The top level container for all blocks */
    private TopLevel toplevel;
    
    private ConnectionCreationManager connectionCreationManager;
    
    private GhciSession ghci;
    private InspectorWindow inspector;
    private PreferencesWindow preferences;

    private Point2D dragStart;
    private Point2D offset;
    
    /** Boolean to indicate that a drag (pan) action has started, yet not finished. */
    private boolean dragging;

    /** The File we're currently working on, if any. */
    private Optional<File> currentFile;

    /**
     * Constructs a new instance.
     */
    public CustomUIPane() {
        super();
        this.bottomLayer = new Pane();
        this.blockLayer = new Pane(this.bottomLayer);
        this.wireLayer = new Pane(this.blockLayer);
        this.getChildren().add(this.wireLayer);

        this.toplevel = new TopLevel(this);
        this.connectionCreationManager = new ConnectionCreationManager(this);
        this.dragStart = Point2D.ZERO;
        this.offset = Point2D.ZERO;

        this.ghci = new GhciSession();
        this.ghci.startAsync();
        
        this.currentFile = Optional.empty();

        this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePress);
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDrag);
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseRelease);
        this.addEventHandler(TouchEvent.TOUCH_PRESSED, this::handleTouchPress);
    }

    public void showInspector() {
        if (inspector == null) {
            inspector = new InspectorWindow(this);
        }

        inspector.show();
    }

    public void showPreferences() {
        if (preferences == null) {
            preferences = new PreferencesWindow(this);
        }

        preferences.show();
    }

    private void handleMousePress(MouseEvent e) {
        if (e.isPrimaryButtonDown() && !e.isSynthesized()) {
            offset = new Point2D(this.getTranslateX(), this.getTranslateY());
            dragStart = new Point2D(e.getScreenX(), e.getScreenY());
            dragging = true;
        }
    }

    private void handleMouseDrag(MouseEvent e) {
    	if (e.isSynthesized()) {
    		return;
    	}
    	
        if (!e.isSecondaryButtonDown()) {
            if (dragging) {
                Point2D dragCurrent = new Point2D(e.getScreenX(), e.getScreenY());
                Point2D delta = dragStart.subtract(dragCurrent);
    
                this.setTranslateX(offset.getX() - delta.getX());
                this.setTranslateY(offset.getY() - delta.getY());
            } else {
                dragStart = new Point2D(e.getScreenX(), e.getScreenY());
                dragging = true;
            }
        }
    }
    
    private void handleMouseRelease(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            if (!e.isSynthesized()) {
                dragging = false;
            }
        } else if (!dragging) {
            this.showFunctionMenuAt(e.getX(), e.getY(), true);
        }
    }

    /** Shows a new function menu at the specified location in this pane. */
    public void showFunctionMenuAt(double x, double y, boolean byMouse) {
        ghci.awaitRunning();
        boolean verticalCurry = this.preferences != null && this.preferences.verticalCurry.isSelected();
        FunctionMenu menu = new FunctionMenu(byMouse, ghci.getCatalog(), this, verticalCurry);
        double verticalCenter = 150; // just a guesstimate, because computing it here is annoying
        menu.relocate(x, y - verticalCenter);
        this.addMenu(menu);
    	
    }
    
    private void handleTouchPress(TouchEvent e) {
    	this.getChildren().add(new TouchArea(e.getTouchPoint()));
    	e.consume();
    }
    
    /** A circular local area for handling multi finger touch actions. */
    private class TouchArea extends Circle {
    	/** The ID of finger that spawned this touch area. */
    	private int touchID;
    	
    	/** Whether this touch area has been dragged further than the drag threshold. */
    	private boolean dragStarted;
    	
    	/** Timed delay for the removal of this touch area. */
    	private Timeline removeDelay;
    	
    	/** Timed delay for the creation of the function menu. */
    	private Timeline menuDelay;
    	
    	/**
    	 * @param touchPoint that is the center of new active touch area.
    	 */
		private TouchArea(TouchPoint touchPoint) {
			super(touchPoint.getX(), touchPoint.getY(), 100, Color.TRANSPARENT);
			this.touchID = touchPoint.getId();
			this.dragStarted = false;
			
			this.removeDelay = new Timeline(new KeyFrame(Duration.millis(100), this::remove));
	    	this.menuDelay = new Timeline(new KeyFrame(Duration.millis(250), this::finishMenu));
	    	
	    	touchPoint.grab(this);
	    	this.addEventHandler(TouchEvent.TOUCH_RELEASED, this::handleRelease);
	    	this.addEventHandler(TouchEvent.TOUCH_PRESSED, this::handlePress);
	    	this.addEventHandler(TouchEvent.TOUCH_MOVED, this::handleDrag);
		}
    	
		private void remove(ActionEvent event) {
			CustomUIPane.this.getChildren().remove(this);
		}
		
		private void finishMenu(ActionEvent event) {
			CustomUIPane.this.showFunctionMenuAt(this.getCenterX(), this.getCenterY(), false);
			CustomUIPane.this.getChildren().remove(this);
		}
		
		private void handlePress(TouchEvent event) {
			// this might have been a drag glitch, so halt release actions
			this.removeDelay.stop();
			this.menuDelay.stop();
			
			event.consume();
		}
		
    	private void handleRelease(TouchEvent event) {
    		if (event.getTouchPoint().getId() != this.touchID && !this.dragStarted) {
    			// trigger menu creation timer
    			this.menuDelay.play();
    		} else if (event.getTouchPoints().stream().filter(tp -> tp.belongsTo(this)).count() == 1) {
    			// trigger area removal timer
    			this.removeDelay.play();
    		}
    		
    		event.consume();
    	}
    	
    	private void handleDrag(TouchEvent event) {
    		if (event.getTouchPoint().getId() != this.touchID) {
    			// we use only primary finger for drag movement
    		} else if (event.getTouchPoints().stream().filter(tp -> tp.belongsTo(this)).count() < 2) {
    			// not a multi finger drag
    		} else {
    			double deltaX = event.getTouchPoint().getX() - this.getCenterX();
    			double deltaY = event.getTouchPoint().getY() - this.getCenterY();
    			
    			if (Math.abs(deltaX) + Math.abs(deltaY) < 2) {
    				// ignore very small movements
                } else if ((deltaX*deltaX + deltaY*deltaY) > 10000) {
                    // FIXME: ignore too large movements
                } else if (this.dragStarted || (deltaX*deltaX + deltaY*deltaY) > 10) {
    				this.dragStarted = true;
    				CustomUIPane.this.setTranslateX(CustomUIPane.this.getTranslateX() + deltaX);
    				CustomUIPane.this.setTranslateY(CustomUIPane.this.getTranslateY() + deltaY);
    			}
    		}
    		
    		event.consume();
    	}
    	
    }
    
    private void setScale(double scale) {
        this.setScaleX(scale);
        this.setScaleY(scale);
    }

    /**
     * @return The Env instance to be used within this CustomUIPane.
     */
    public Environment getEnvInstance() {
        ghci.awaitRunning();
        return ghci.getCatalog().asEnvironment();
    }

    /** @return the top level block container */
    public TopLevel getTopLevel() {
        return this.toplevel;
    }
    
    /** Remove the given block from this UI pane, including its connections. */
    public void removeBlock(Block block) {
        block.deleteAllLinks();
        
        if (block.belongsOnBottom()) {
            this.bottomLayer.getChildren().remove(block);
        } else {
            this.blockLayer.getChildren().remove(block);
        }
    }

    /** Attempts to create a copy of a block and add it to this pane. */
    public void copyBlock(Block block) {
        block.getNewCopy().ifPresent(copy -> {
          this.addBlock(copy);
          copy.relocate(block.getLayoutX()+20, block.getLayoutY()+20);
          copy.initiateConnectionChanges();
        });
    }
    
    
    public ConnectionCreationManager getConnectionCreationManager() {
        return connectionCreationManager;
    }

    public GhciSession getGhciSession() {
        return ghci;
    }

    public void zoomOut() {
        zoom(0.8);
    }

    public void zoomIn() {
        zoom(1.25);
    }

    private void zoom(double ratio) {
        double scale = this.getScaleX();

        /* Limit zoom to reasonable range. */
        if (scale <= 0.2 && ratio < 1) return;
        if (scale >= 3 && ratio > 1) return;

        this.setScale(scale * ratio);
        this.setTranslateX(this.getTranslateX() * ratio);
        this.setTranslateY(this.getTranslateY() * ratio);
    }

    /** Gets the file we're currently working on, if any. */
    public Optional<File> getCurrentFile() {
        return currentFile;
    }

    /**
     * Sets the file we're currently working on. Probably called from a Save
     * As/Open operation.
     */
    public void setCurrentFile(File currentFile) {
        this.currentFile = Optional.of(currentFile);
    }

    /**
     * Terminate the current GhciSession, if any, then start a new one.
     * Waits for the old session to end, but not for the new session to start.
     */
    public void restartBackend() {
        ghci.stopAsync();
        ghci.awaitTerminated();

        ghci = new GhciSession();
        ghci.startAsync();
    }

    public void addBlock(Block block) {
        if (block.belongsOnBottom()) {
            this.bottomLayer.getChildren().add(block);
        } else {
            this.blockLayer.getChildren().add(block);
        }
    }

    public boolean addMenu(FunctionMenu menu) {
        return this.getChildren().add(menu);
    }

    public boolean removeMenu(FunctionMenu menu) {
        return this.getChildren().remove(menu);
    }

    public boolean addConnection(Connection connection) {
        return this.wireLayer.getChildren().add(connection);
    }

    public boolean removeConnection(Connection connection) {
        return this.wireLayer.getChildren().remove(connection);
    }

    public boolean addWire(DrawWire drawWire) {
        return this.getChildren().add(drawWire);
    }

    public boolean removeWire(DrawWire drawWire) {
        return this.getChildren().remove(drawWire);
    }

    public void clearChildren() {
        this.bottomLayer.getChildren().clear();
        this.blockLayer.getChildren().remove(1, this.blockLayer.getChildren().size());
        this.wireLayer.getChildren().remove(1, this.blockLayer.getChildren().size());
        this.toplevel = new TopLevel(this);
    }

    public Stream<Node> streamChildren() {
        Stream<Node> bottom = this.bottomLayer.getChildren().stream();
        Stream<Node> blocks = this.blockLayer.getChildren().stream().skip(1);
        Stream<Node> wires  = this.wireLayer.getChildren().stream().skip(1);

        return Stream.concat(bottom, Stream.concat(blocks, wires));
    }

    public Stream<BlockContainer> getBlockContainers() {
        return bottomLayer.getChildrenUnmodifiable().stream().flatMap(node ->
            (node instanceof Block) ? ((Block)node).getInternalContainers().stream() : Stream.empty());
    }

    /**
     * Ensures that the ordering of container blocks on the bottom layer is consistent with parent ordering.
     * @param block that might need corrections in the visual ordering. 
     */
    public void moveInFrontOfParentContainers(Block block) {
        if (block.getContainer() instanceof WrappedContainer) {
            Block parent = ((WrappedContainer)block.getContainer()).getWrapper();
            int childIndex = this.bottomLayer.getChildren().indexOf(block);
            int parentIndex = this.bottomLayer.getChildren().indexOf(parent);
            if (childIndex < parentIndex && childIndex >= 0) {
                this.bottomLayer.getChildren().remove(block);
                this.bottomLayer.getChildren().add(parentIndex, block);
                // moving the block after the parent might have caused ordering issues in the block inbetween, resolve them
                for (Node node : new ArrayList<Node>(this.bottomLayer.getChildren().subList(childIndex, parentIndex-1))) {
                    if (node instanceof Block) {
                        this.moveInFrontOfParentContainers((Block)node);
                    }
                }
            }
        }
    }
    
}
