package nl.utwente.viskell.ui;

import com.google.common.base.Splitter;

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
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.env.CatalogFunction;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.components.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private CustomUIPane parent;
    @FXML
    private Pane searchSpace;
    @FXML
    private Pane categorySpace;
    @FXML
    private Pane utilSpace;

    public FunctionMenu(HaskellCatalog catalog, CustomUIPane pane) {
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
                            if (e.getButton() == MouseButton.SECONDARY) {
                                if (entry.isConstructor()) {
                                    addBlock(new MatchBlock(entry, parent));
                                } else {
                                    addBlock(new FunApplyBlock(entry, parent));
                                }
                            } else {
                                addBlock(new FunctionBlock(entry, parent));
                            }
                        });
                    }

                    @Override
                    protected void updateItem(CatalogFunction item, boolean empty) {
                        super.updateItem(item, empty);
                        this.setText(item == null ? null : item.getName());
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
        closeButton.setOnAction(event -> close());
        Button valBlockButton = new Button("Value Block");
        valBlockButton.setOnAction(event -> addValueBlock());
        Button disBlockButton = new Button("Display Block");
        disBlockButton.setOnAction(event -> addBlock(new DisplayBlock(parent)));
        Button defBlockButton = new Button("Definition Block");
        defBlockButton.setOnAction(event -> addDefinitionBlock());
        Button lambdaBlockButton = new Button("Lambda Block");
        lambdaBlockButton.setOnAction(event -> addLambdaBlock());
        utilSpace.getChildren().addAll(closeButton, valBlockButton, disBlockButton, defBlockButton, lambdaBlockButton);

        if (GhciSession.pickBackend() == GhciSession.Backend.GHCi) {
            // These blocks are specifically for GHCi
            Button sliderBlockButton = new Button("Slider Block");
            sliderBlockButton.setOnAction(event -> addBlock(new SliderBlock(parent)));
            Button rgbBlockButton = new Button("RGB Block");
            rgbBlockButton.setOnAction(event -> addBlock(new RGBBlock(parent)));
            Button graphBlockButton = new Button("Graph Block");
            graphBlockButton.setOnAction(event -> addBlock(new GraphBlock(parent)));

            utilSpace.getChildren().addAll(sliderBlockButton, rgbBlockButton, graphBlockButton);
        }

        if (GhciSession.pickBackend() == GhciSession.Backend.Clash) {
            // These blocks are specifically for Clash

            Button simulateBlockButton = new Button("Simulate Block");
            simulateBlockButton.setOnAction(event -> addBlock(new SimulateBlock(parent)));

            utilSpace.getChildren().addAll(simulateBlockButton);
        }

        for (Node button : utilSpace.getChildren()) {
            ((Region) button).setMaxWidth(Double.MAX_VALUE);
        }

    }

    private void addValueBlock() {
        TextInputDialog dialog = new TextInputDialog("Value");
        dialog.setTitle("Add value block");
        dialog.setHeaderText("Add value block");
        dialog.setContentText("Value");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(value -> {
            GhciSession ghci = parent.getGhciSession();

            try {
                Type type = ghci.pullType(value, parent.getEnvInstance());
                ValueBlock val = new ValueBlock(this.parent, type, value);
                addBlock(val);
            } catch (HaskellException e) {
                // Retry.
                addValueBlock();
            }
        });
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
    
    private void addBlock(Block block) {
        parent.addBlock(block);
        Point2D pos = this.localToParent(0, 0);
        block.relocate(pos.getX() - 200, pos.getY());
        block.initiateConnectionChanges();
    }

    /** Closes this menu by removing it from it's parent. */
    public void close() {
        parent.removeMenu(this);
    }
}
