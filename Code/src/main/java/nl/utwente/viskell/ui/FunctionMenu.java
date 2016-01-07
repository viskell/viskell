package nl.utwente.viskell.ui;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.env.CatalogFunction;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.components.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * FunctionMenu is a viskell specific menu implementation. A FunctionMenu is an
 * always present menu, once called it will remain open until the
 * {@linkplain #close() } method is called. An application can have multiple
 * instances of FunctionMenu where each menu maintains it's own state.
 * <p>
 * FunctionMenu is constructed out of three different spaces:
 * {@code searchSpace}, {@code categorySpace} and {@code utilSpace}. The
 * {@code searchSpace} should be used to display search components that can be
 * used to find stored functions more quickly, {@code categorySpace} contains an
 * {@linkplain Accordion} where each category of functions is displayed in their
 * own {@linkplain TitledPane}. {@code utilSpace} can then contain any form of
 * utility methods or components that might need quick accessing.
 * </p>
 */
public class FunctionMenu extends StackPane implements ComponentLoader {

    /** The context that deals with dragging for this Menu */
    protected DragContext dragContext;

    /** Keep track of how many blocks are placed from this menu */
    private int blockCounter = 0;
    
    private Accordion categoryContainer = new Accordion();
    private ToplevelPane parent;
    @FXML
    private Pane searchSpace;
    @FXML
    private Pane categorySpace;
    @FXML
    private Pane utilSpace;

    public FunctionMenu(boolean byMouse, HaskellCatalog catalog, ToplevelPane pane) {
        this.parent = pane;
        this.loadFXML("FunctionMenu");
        this.dragContext = new DragContext(this);
        /*
         * Consume scroll events to prevent mixing of zooming and list
         * scrolling.
         */
        this.addEventHandler(ScrollEvent.SCROLL, Event::consume);

        /* Create content for searchSpace. */
        // TODO create search tools and add them to searchSpace.

        /* Create content for categorySpace. */
        ArrayList<String> categories = new ArrayList<>(catalog.getCategories());
        categories.add("Deconstructors");
        Collections.sort(categories);

        for (String category : categories) {
            ObservableList<CatalogFunction> items = FXCollections.observableArrayList();

            ArrayList<CatalogFunction> entries; 
            if ("Deconstructors".equals(category)) {
                entries = categories.stream().flatMap(c -> 
                        catalog.getCategory(c).stream()).filter(e -> e.isConstructor())
                        .collect(Collectors.toCollection(() -> new ArrayList<>()));
            } else {
                entries = new ArrayList<>(catalog.getCategory(category));
            }
            Collections.sort(entries);
            items.addAll(entries);

            ListView<CatalogFunction> listView = new ListView<>(items);

            listView.setCellFactory((list) -> {
                return new ListCell<CatalogFunction>() {
                    {
                        
                        this.setOnMouseReleased(e -> {
                            if (this.isEmpty()) {
                                return;
                            }
                            
                            if ((e.isSynthesized() && e.getButton() != MouseButton.SECONDARY) || !this.contains(e.getX(), e.getY())) {
                                return;
                            }
                            
                            CatalogFunction entry = this.getItem();
                            if ("Deconstructors".equals(category) && entry.isConstructor()) {
                                addBlock(new MatchBlock(entry, parent));
                            } else if (e.getButton() == MouseButton.SECONDARY && entry.isConstructor()) {
                                addBlock(new MatchBlock(entry, parent));
                            } else if (!(entry.getFreshSignature() instanceof FunType)) {
                                addBlock(new ConstantBlock(pane, entry.getFreshSignature(), entry.getName(), true));
                            } else if (e.isControlDown() || Preferences.userNodeForPackage(Main.class).getBoolean("verticalCurry", true)) {
                            	if (entry.getName().startsWith("(") && entry.getFreshSignature().countArguments() == 2) {
                            		addBlock(new BinOpApplyBlock(entry, parent));
                            	} else {
                            		addBlock(new FunApplyBlock(new LibraryFunUse(entry), parent));
                            	}
                            } else {
                                addBlock(new FunctionBlock(new LibraryFunUse(entry), parent));
                            }
                        });
              
                        final double[] touchStartY = new double[]{0.0};
                        
                        this.setOnTouchPressed(e -> {
                            touchStartY[0] = this.localToParent(e.getTouchPoint().getX(), e.getTouchPoint().getY()).getY();
                        });
                        
                        this.setOnTouchReleased(e -> {
                            if (this.isEmpty()) {
                                return;
                            }
                            
                            double touchParentY = this.localToParent(e.getTouchPoint().getX(), e.getTouchPoint().getY()).getY();
                            if (Math.abs(touchStartY[0] - touchParentY) > 10) {
                                // a release after scrolling is not intended as touch click
                                return;
                            }
                            
                            if (!this.contains(e.getTouchPoint().getX(), e.getTouchPoint().getY())) {
                                return;
                            }
                            
                            CatalogFunction entry = this.getItem();
                            
                            if ("Deconstructors".equals(category) && entry.isConstructor()) {
                                addBlock(new MatchBlock(entry, parent));
                            } else if (!(entry.getFreshSignature() instanceof FunType)) {
                                addBlock(new ConstantBlock(pane, entry.getFreshSignature(), entry.getName(), true));
                            } else if (e.isControlDown() || Preferences.userNodeForPackage(Main.class).getBoolean("verticalCurry", true)) {
                                if (entry.getName().startsWith("(") && entry.getFreshSignature().countArguments() == 2) {
                                    addBlock(new BinOpApplyBlock(entry, parent));
                                } else {
                                    addBlock(new FunApplyBlock(new LibraryFunUse(entry), parent));
                                }
                            } else {
                                addBlock(new FunctionBlock(new LibraryFunUse(entry), parent));
                            }
                        });
                        
                        this.setOnTouchMoved(e -> {
                            if (this.isEmpty()) {
                                return;
                            }
          
                            if (this.contains(e.getTouchPoint().getX(), e.getTouchPoint().getY())) {
                                return;
                            }
                            
                            double sceneX = e.getTouchPoint().getSceneX();
                            Bounds bounds = FunctionMenu.this.localToScene(FunctionMenu.this.getBoundsInLocal());
                            if (sceneX < bounds.getMinX()-75 || sceneX > bounds.getMaxX()+25) {
                                CatalogFunction entry = this.getItem();
                                if ("Deconstructors".equals(category) && entry.isConstructor()) {
                                    addDraggedBlock(e.getTouchPoint(), new MatchBlock(entry, parent));
                                } else if (!(entry.getFreshSignature() instanceof FunType)) {
                                    addDraggedBlock(e.getTouchPoint(), new ConstantBlock(pane, entry.getFreshSignature(), entry.getName(), true));
                                } else if (e.isControlDown() || Preferences.userNodeForPackage(Main.class).getBoolean("verticalCurry", true)) {
                                    if (entry.getName().startsWith("(") && entry.getFreshSignature().countArguments() == 2) {
                                        addDraggedBlock(e.getTouchPoint(), new BinOpApplyBlock(entry, parent));
                                    } else {
                                        addDraggedBlock(e.getTouchPoint(), new FunApplyBlock(new LibraryFunUse(entry), parent));
                                    }
                                } else {
                                    addDraggedBlock(e.getTouchPoint(), new FunctionBlock(new LibraryFunUse(entry), parent));
                                }
                                e.consume();
                            }
                        });
                    }

                    @Override
                    protected void updateItem(CatalogFunction item, boolean empty) {
                        super.updateItem(item, empty);
                        this.setText(item == null ? null : item.getDisplayName());
                    }

                };
            });

            
            TitledPane submenu = new TitledPane(category, listView);
            submenu.setAnimated(false);
            
            // toggling of the submenu by touch
            submenu.setOnTouchReleased(event -> {
                if (event.getTouchPoint().getY() < 24) {
                    submenu.setExpanded(! submenu.isExpanded());
                }
            });
            
            //Prevent dragging the whole menu when dragging inside a category list 
            submenu.addEventHandler(TouchEvent.TOUCH_MOVED, Event::consume);
            
            categoryContainer.getPanes().addAll(submenu);
        }

        // drop all synthesized mouse events
        categoryContainer.addEventFilter(MouseEvent.ANY, e -> {if (e.isSynthesized()) e.consume();});
        
        // Hiding all other categories on expanding one of them.
        List<TitledPane> allCatPanes = new ArrayList<>(categoryContainer.getPanes());
        categoryContainer.expandedPaneProperty().addListener(e -> {
            TitledPane expPane = categoryContainer.getExpandedPane();
            if (expPane != null) {
                categoryContainer.getPanes().retainAll(expPane);
            } else {
                categoryContainer.getPanes().clear();
                categoryContainer.getPanes().addAll(allCatPanes);
            }
        });
        
        this.categorySpace.getChildren().add(categoryContainer);

        /* Create content for utilSpace. */
        Button closeButton = new MenuButton("Close", bm -> close(bm));
        Button valBlockButton = new MenuButton("Constant", bm -> addConstantBlock());
        Button arbBlockButton = new MenuButton("Arbitrary", bm -> addBlock(new ArbitraryBlock(parent)));
        Button disBlockButton = new MenuButton("Observe", bm -> addBlock(new DisplayBlock(parent)));
        Button lambdaBlockButton = new MenuButton("Lambda", bm -> addLambdaBlock());
        Button choiceBlockButton = new MenuButton("Choice", bm -> addChoiceBlock());
        Button applyBlockButton = new MenuButton("Apply", bm -> addBlock(new FunApplyBlock(new ApplyAnchor(1), parent)));
        

        utilSpace.getChildren().addAll(closeButton, disBlockButton, arbBlockButton, valBlockButton, lambdaBlockButton, applyBlockButton, choiceBlockButton);

        if (GhciSession.pickBackend() == GhciSession.Backend.GHCi) {
            // These blocks are specifically for GHCi
            Button rationalBlockButton = new MenuButton("Rational", bm -> addBlock(new SliderBlock(parent, false)));
            Button IntegerBlockButton = new MenuButton("Integer", bm -> addBlock(new SliderBlock(parent, true)));
            Button graphBlockButton = new MenuButton("Graph", bm -> addBlock(new GraphBlock(parent)));

            utilSpace.getChildren().addAll(graphBlockButton, IntegerBlockButton, rationalBlockButton);
        }

        if (GhciSession.pickBackend() == GhciSession.Backend.Clash) {
            // These blocks are specifically for Clash

            Button simulateBlockButton = new MenuButton("Simulate", bm -> addBlock(new SimulateBlock(parent)));

            utilSpace.getChildren().addAll(simulateBlockButton);
        }

        // with an odd number of block buttons fill the last spot with a close button
        if (utilSpace.getChildren().size() % 2 == 1) {
            Button extraCloseButton = new MenuButton("Close", bm -> close(bm));
            utilSpace.getChildren().add(extraCloseButton);
        }
        

        for (Node button : utilSpace.getChildren()) {
            ((Region) button).setMaxWidth(Double.MAX_VALUE);
        }

        // opening animation of this menu, during which it can't be accidentally used
        this.setMouseTransparent(true);
        this.setScaleX(0.3);
        this.setScaleY(0.1);
        ScaleTransition opening = new ScaleTransition(byMouse ? Duration.ONE : Duration.millis(300), this);
        opening.setToX(1);
        opening.setToY(1);
        opening.setOnFinished(e -> this.setMouseTransparent(false));
        opening.play();
    }

    /** Specialized Button that behaves better in a many touch environment. */
    private static class MenuButton extends Button {
        
        private int touchDragCounter;

        private MenuButton(String text, Consumer<Boolean> action) {
            super(text);
            this.touchDragCounter = 0;
            this.getStyleClass().add("menuButton");
            this.setOnMouseClicked(event -> {if (!event.isSynthesized()) action.accept(true);});

            Timeline dragReset = new Timeline(new KeyFrame(Duration.millis(500), e -> this.touchDragCounter = 0));
            this.setOnTouchReleased(event -> {if (this.touchDragCounter < 7) action.accept(false);});
            this.setOnTouchPressed(event -> dragReset.play());
            this.setOnTouchMoved(event -> this.touchDragCounter++);
        }
    }
    
    private void addConstantBlock() {
        ConstantBlock val = new ConstantBlock(this.parent);
        addBlock(val);
        val.editValue(Optional.of("\"Hello, World!\""));
    }

    private void addLambdaBlock() {
        addBlock(new LambdaBlock(this.parent, 1));
    }
    
    private void addChoiceBlock() {
        ChoiceBlock def = new ChoiceBlock(this.parent);
        addBlock(def);
    }
    
    private void addBlock(Block block) {
        parent.addBlock(block);
        Bounds menuBounds = this.getBoundsInParent();
        int offSetY = (this.blockCounter % 5) * 20 + (block.getAllOutputs().isEmpty() ? 250 : 125);
        if (this.localToScene(Point2D.ZERO).getX() < 200) {
            // too close to the left side of screen, put block on the right
            int offSetX = (this.blockCounter % 5) * 10 + (block.belongsOnBottom() ? 50 : 100);
            block.relocate(menuBounds.getMaxX() + offSetX, menuBounds.getMinY()+ offSetY);
        } else {
            int offSetX = (this.blockCounter % 5) * 10 - (block.belongsOnBottom() ? 400 : 200);
            block.relocate(menuBounds.getMinX() + offSetX, menuBounds.getMinY()+ offSetY);
        }

        if (! block.belongsOnBottom()) {
            block.refreshContainer();
        }
        
        block.initiateConnectionChanges();
        this.blockCounter++;
    }

    private void addDraggedBlock(TouchPoint touchPoint, Block block) {
        Point2D pos = parent.sceneToLocal(touchPoint.getSceneX(), touchPoint.getSceneY());
        parent.addBlock(block);
        block.relocate(pos.getX(), pos.getY());
        touchPoint.grab(block);
        block.initiateConnectionChanges();
    }

    /** Closes this menu by removing it from it's parent. */
    public void close(boolean byMouse) {
    	// disable it first, before removal in a closing animation 
        this.setMouseTransparent(true);
        ScaleTransition closing = new ScaleTransition(byMouse ? Duration.ONE :Duration.millis(300), this);
        closing.setToX(0.3);
        closing.setToY(0.1);
        closing.setOnFinished(e -> parent.removeMenu(this));
        closing.play();
    }
}
