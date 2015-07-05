package nl.utwente.group10.ui.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import nl.utwente.group10.haskell.catalog.Context;
import nl.utwente.group10.haskell.catalog.FunctionEntry;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.typeparser.TypeBuilder;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.blocks.*;

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
        this.addEventHandler(ScrollEvent.SCROLL, Event::consume);

        /* Create content for searchSpace. */
        // TODO create search tools and add them to searchSpace.

        /* Create content for categorySpace. */
        ArrayList<String> categories = new ArrayList<>(catalog.getCategories());
        Collections.sort(categories);

        for (String category : categories) {
            ObservableList<FunctionEntry> items = FXCollections.observableArrayList();

            ArrayList<FunctionEntry> entries = new ArrayList<>(catalog.getCategory(category));
            Collections.sort(entries);

            for (FunctionEntry entry : entries) {
                items.add(entry);
            }

            ListView<FunctionEntry> listView = new ListView<FunctionEntry>(items);
            listView.getSelectionModel().selectedItemProperty().addListener((c, oV, nV) -> {
                        addFunctionBlock(nV);
                    });

            listView.setCellFactory((list) -> {
                return new ListCell<FunctionEntry>() {
                    @Override
                    protected void updateItem(FunctionEntry item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            });

            TitledPane submenu = new TitledPane(category, listView);
            submenu.setAnimated(false);
            categoryContainer.getPanes().addAll(submenu);
        }

        this.categorySpace.getChildren().add(categoryContainer);

        /* Create content for utilSpace. */
        Button valBlockButton = new Button("Value Block");
        valBlockButton.setOnAction(event -> addValueBlock());
        Button disBlockButton = new Button("Display Block");
        disBlockButton.setOnAction(event -> addBlock(new DisplayBlock(parent)));
        Button sliderBlockButton = new Button("Slider Block");
        sliderBlockButton.setOnAction(event -> addBlock(new SliderBlock(parent)));
        Button rgbBlockButton = new Button("RGB Block");
        rgbBlockButton.setOnAction(event -> addBlock(new RGBBlock(parent)));
        Button graphBlockButton = new Button("Graph Block");
        graphBlockButton.setOnAction(event -> addBlock(new GraphBlock(parent)));

        // TODO remove once debugging is done
        Button debugButton = new Button("Error All");
        debugButton.setOnAction(event -> parent.errorAll());
        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());

        utilSpace.getChildren().addAll(
            valBlockButton,
            disBlockButton,
            sliderBlockButton,
            rgbBlockButton,
            graphBlockButton,
            debugButton,
            closeButton
        );

        for (Node button : utilSpace.getChildren()) {
            ((Region) button).setMaxWidth(Double.MAX_VALUE);
        }

    }

    private void addFunctionBlock(FunctionEntry entry) {
        // TODO: Once the Env is available, the type should be pulled from the
        // Env here (don't just calculate it over and over). Or just pass the
        // signature String.
        FunctionBlock fb = new FunctionBlock(entry.getName(), entry.asHaskellObject(new Context()), parent);
        addBlock(fb);
    }

    private void addValueBlock() {
        TextInputDialog dialog = new TextInputDialog("Value");
        dialog.setTitle("Add value block");
        dialog.setHeaderText("Add value block");
        dialog.setContentText("Value");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(value -> {
            parent.getGhciSession().ifPresent(ghci -> {
                try {
                    String t = ghci.pull(new Ident(":t " + value)).split(" :: ")[1].trim();
                    Type type = new TypeBuilder(parent.getEnvInstance().getTypeClasses()).build(t);

                    ValueBlock val = new ValueBlock(this.parent, type, value);
                    addBlock(val);
                } catch (HaskellException | ArrayIndexOutOfBoundsException e) {
                    // Retry.
                    addValueBlock();
                }
            });
        });
    }

    private void addBlock(Block block) {
        parent.getChildren().add(block);
        Point2D pos = this.localToParent(0, 0);
        block.relocate(pos.getX() - 200, pos.getY());
    }

    /** Closes this menu by removing it from it's parent. */
    public void close() {
        parent.getChildren().remove(this);
    }
}
