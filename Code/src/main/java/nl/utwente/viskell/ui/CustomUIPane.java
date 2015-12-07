package nl.utwente.viskell.ui;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.ui.components.Block;
import nl.utwente.viskell.ui.components.BlockContainer;
import nl.utwente.viskell.ui.components.ChoiceBlock;
import nl.utwente.viskell.ui.components.DefinitionBlock;
import nl.utwente.viskell.ui.components.InputAnchor;

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
    
    private ObjectProperty<Optional<Block>> selectedBlock;
    private ConnectionCreationManager connectionCreationManager;
    
    private GhciSession ghci;
    private InspectorWindow inspector;
    private PreferencesWindow preferences;

    private Point2D dragStart;
    private Point2D offset;
    
    /** Boolean to indicate that a drag (pan) action has started, yet not finished. */
    private boolean dragging;

    private HaskellCatalog catalog;
    private Environment envInstance;

    /** The File we're currently working on, if any. */
    private Optional<File> currentFile;

    /**
     * Constructs a new instance.
     */
    public CustomUIPane(HaskellCatalog catalog) {
        super();
        this.bottomLayer = new Pane();
        this.blockLayer = new Pane(this.bottomLayer);
        this.wireLayer = new Pane(this.blockLayer);
        this.getChildren().add(this.wireLayer);
        
        this.connectionCreationManager = new ConnectionCreationManager(this);
        this.selectedBlock = new SimpleObjectProperty<>(Optional.empty());
        this.dragStart = Point2D.ZERO;
        this.offset = Point2D.ZERO;
        this.catalog = catalog;
        this.envInstance = catalog.asEnvironment();

        this.ghci = new GhciSession();
        this.ghci.startAsync();

        this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handlePress);
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleDrag);
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleRelease);
        this.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKey);
    }

    private void handleKey(KeyEvent keyEvent) {
        int dist = 100;

        switch (keyEvent.getCode()) {
            case UP:     this.setTranslateY(this.getTranslateY() + dist); break;
            case DOWN:   this.setTranslateY(this.getTranslateY() - dist); break;
            case LEFT:   this.setTranslateX(this.getTranslateX() + dist); break;
            case RIGHT:  this.setTranslateX(this.getTranslateX() - dist); break;

            case H: // C&C-style
            case BACK_SPACE: // SC-style
                this.setTranslateX(0);
                this.setTranslateY(0);
                break;

            case EQUALS: this.setScale(this.getScaleX() * 1.25); break;
            case MINUS:  this.setScale(this.getScaleX() * 0.8); break;
            case DIGIT1: this.setScale(1); break;

            case Z:
                showInspector();
                break;

            case DELETE:
                removeSelected();
                break;
            default:
                break;
        }
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

    private void handlePress(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            offset = new Point2D(this.getTranslateX(), this.getTranslateY());
            dragStart = new Point2D(e.getScreenX(), e.getScreenY());
            dragging = true;
        } else if (e.isSecondaryButtonDown()) {
            FunctionMenu menu = new FunctionMenu(catalog, this);
            menu.relocate(e.getX(), e.getY());
            this.addMenu(menu);
        }
    }

    private void handleDrag(MouseEvent e) {
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
    
    private void handleRelease(MouseEvent e) {
        dragging = false;
    }

    private void setScale(double scale) {
        this.setScaleX(scale);
        this.setScaleY(scale);
    }

    /**
     * @return The Env instance to be used within this CustomUIPane.
     */
    public Environment getEnvInstance() {
        return envInstance;
    }

    public Optional<Block> getSelectedBlock() {
        return selectedBlock.get();
    }

    public void setSelectedBlock(Block selectedBlock) {
        this.selectedBlock.set(Optional.ofNullable(selectedBlock));
    }

    public ObjectProperty<Optional<Block>> selectedBlockProperty() {
        return selectedBlock;
    }
    
    /** Remove the given block from this UI pane, including its connections. */
    public void removeBlock(Block block) {
        block.getAllInputs().forEach(input -> input.removeConnections());
        block.getAllOutputs().forEach(output -> output.removeConnections());
        block.refreshContainer();
        
        if (block.belongsOnBottom()) {
            this.bottomLayer.getChildren().remove(block);
        } else {
            this.blockLayer.getChildren().remove(block);
        }
    }

    /** Remove the selected block, if any. */
    private void removeSelected() {
        this.getSelectedBlock().ifPresent(this::removeBlock);
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

    public void addMenu(Node menu) {
        this.getChildren().add(menu);
    }
    
    public void removeMenu(Node menu) {
        this.getChildren().remove(menu);
    }

    public void addConnection(Node connection) {
        this.wireLayer.getChildren().add(connection);
    }

    public void removeConnection(Node connection) {
       this.wireLayer.getChildren().remove(connection);
    }

    public void addWire(Node drawWire) {
        this.getChildren().add(drawWire);
    }

    public void removeWire(Node drawWire) {
        this.getChildren().remove(drawWire);
    }
    
    public void clearChildren() {
        this.bottomLayer.getChildren().clear();
        this.blockLayer.getChildren().remove(1, this.blockLayer.getChildren().size());
        this.wireLayer.getChildren().remove(1, this.blockLayer.getChildren().size());
    }

    public Stream<Node> streamChildren() {
        return Stream.concat(this.bottomLayer.getChildren().stream(), Stream.concat(
                this.blockLayer.getChildren().stream().skip(1), this.wireLayer.getChildren().stream().skip(1)));
    }
    
    public Stream<BlockContainer> getBlockContainers() {
        return bottomLayer.getChildrenUnmodifiable().stream().flatMap(node -> {
            if (node instanceof ChoiceBlock) {
                return ((ChoiceBlock)node).getLanes().stream();
            }
            if (node instanceof DefinitionBlock) {
                return Stream.of(((DefinitionBlock)node).getBody());
            }
            return Stream.empty();
        });
    }
}
