package nl.utwente.viskell.ui;

import com.google.common.base.Splitter;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.env.CatalogFunction;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.components.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

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
        Collections.sort(categories);

        for (String category : categories) {
            ObservableList<CatalogFunction> items = FXCollections.observableArrayList();

            ArrayList<CatalogFunction> entries = new ArrayList<>(catalog.getCategory(category));
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
                            
                            CatalogFunction entry = this.getItem();
                            if (e.getButton() == MouseButton.SECONDARY && entry.isConstructor()) {
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
            
            //Prevent dragging the whole menu when dragging inside a category list 
            submenu.addEventHandler(TouchEvent.ANY, Event::consume);
            
            categoryContainer.getPanes().addAll(submenu);
        }

        this.categorySpace.getChildren().add(categoryContainer);

        /* Create content for utilSpace. */
        Button closeButton = new Button("Close");
        closeButton.setOnMouseClicked(event -> close(true));
        closeButton.setOnTouchPressed(event -> close(false));
        Button valBlockButton = new Button("Constant");
        valBlockButton.setOnAction(event -> addConstantBlock());
        Button arbBlockButton = new Button("Arbitrary");
        arbBlockButton.setOnAction(event -> addBlock(new ArbitraryBlock(parent)));
        Button disBlockButton = new Button("Display");
        disBlockButton.setOnAction(event -> addBlock(new DisplayBlock(parent)));
        Button defBlockButton = new Button("Definition");
        defBlockButton.setOnAction(event -> addDefinitionBlock());
        Button lambdaBlockButton = new Button("Lambda");
        lambdaBlockButton.setOnAction(event -> addLambdaBlock());
        Button choiceBlockButton = new Button("Choice");
        choiceBlockButton.setOnAction(event -> addChoiceBlock());
        Button applyBlockButton = new Button("Apply");
        applyBlockButton.setOnAction(event -> addBlock(new FunApplyBlock(new ApplyAnchor(1), parent)));
        

        utilSpace.getChildren().addAll(closeButton, valBlockButton, arbBlockButton, disBlockButton, defBlockButton, lambdaBlockButton, choiceBlockButton, applyBlockButton);

        if (GhciSession.pickBackend() == GhciSession.Backend.GHCi) {
            // These blocks are specifically for GHCi
            Button rationalBlockButton = new Button("Rational");
            rationalBlockButton.setOnAction(event -> addBlock(new SliderBlock(parent, false)));
            Button IntegerBlockButton = new Button("Integer");
            IntegerBlockButton.setOnAction(event -> addBlock(new SliderBlock(parent, true)));
            Button graphBlockButton = new Button("Graph");
            graphBlockButton.setOnAction(event -> addBlock(new GraphBlock(parent)));

            utilSpace.getChildren().addAll(rationalBlockButton, IntegerBlockButton, graphBlockButton);
        }

        if (GhciSession.pickBackend() == GhciSession.Backend.Clash) {
            // These blocks are specifically for Clash

            Button simulateBlockButton = new Button("Simulate");
            simulateBlockButton.setOnAction(event -> addBlock(new SimulateBlock(parent)));

            utilSpace.getChildren().addAll(simulateBlockButton);
        }

        // with an odd number of block buttons fill the last spot with a close button
        if (utilSpace.getChildren().size() % 2 == 1) {
            Button extraCloseButton = new Button("Close");
            extraCloseButton.setOnMouseClicked(event -> close(true));
            extraCloseButton.setOnTouchPressed(event -> close(false));
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

    private void addConstantBlock() {
        ConstantBlock val = new ConstantBlock(this.parent);
        addBlock(val);
        val.editValue(Optional.of("\"Hello, World!\""));
    }

    /** Add a new definition block (named, typed lambda block) */
    private void addDefinitionBlock() {
        TextInputDialog dialog = new TextInputDialog("example :: Int -> Int");
        dialog.setTitle("Add definition block");
        dialog.setHeaderText("Add function definition");
        dialog.setContentText("Function signature:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(signature -> {
            List<String> parts = Splitter.on(" :: ").splitToList(signature);
            String name = parts.get(0);
            String hs = parts.get(1);

            Type type = parent.getEnvInstance().buildType(hs);

            DefinitionBlock def = new DefinitionBlock(this.parent, name, type);
            addBlock(def);
        });
    }

    private void addLambdaBlock() {
        DefinitionBlock def = new DefinitionBlock(this.parent, 1);
        addBlock(def);
    }
    
    private void addChoiceBlock() {
        ChoiceBlock def = new ChoiceBlock(this.parent);
        addBlock(def);
    }
    
    private void addBlock(Block block) {
        parent.addBlock(block);
        Point2D pos = this.localToParent(0, 0);
        block.relocate(pos.getX() - 200, pos.getY());
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
