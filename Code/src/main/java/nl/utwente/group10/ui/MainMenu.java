package nl.utwente.group10.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import nl.utwente.group10.haskell.catalog.Context;
import nl.utwente.group10.haskell.catalog.FunctionEntry;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.ValueBlock;
import nl.utwente.group10.ui.components.blocks.DisplayBlock;
import nl.utwente.group10.ui.components.blocks.FunctionBlock;

public class MainMenu extends ContextMenu {
    private CustomUIPane parent;

    public MainMenu(HaskellCatalog catalog, CustomUIPane tactilePane) {
        parent = tactilePane;

        ArrayList<String> categories = new ArrayList<>(catalog.getCategories());
        Collections.sort(categories);

        for (String category : categories) {
            Menu submenu = new Menu(category);

            ArrayList<FunctionEntry> entries = new ArrayList<>(catalog.getCategory(category));
            Collections.sort(entries);

            for (FunctionEntry entry : entries) {
                MenuItem item = new MenuItem(entry.getName());
                item.setOnAction(event -> addFunctionBlock(entry));
                submenu.getItems().add(item);
        }

            this.getItems().addAll(submenu);
        }

        MenuItem valueBlockItem = new MenuItem("Value Block");
        valueBlockItem.setOnAction(event -> addValueBlock());
        MenuItem displayBlockItem = new MenuItem("Display Block");
        displayBlockItem.setOnAction(event -> addDisplayBlock());
        
        //TODO remove this item when debugging of visualFeedback is done
        MenuItem errorItem = new MenuItem("Error all Blocks");
        errorItem.setOnAction(event -> parent.errorAll());

        MenuItem quitItem = new MenuItem("Quit");
        quitItem.setOnAction(event -> System.exit(0));

        SeparatorMenuItem sep = new SeparatorMenuItem();

        this.getItems().addAll(valueBlockItem, displayBlockItem, sep, errorItem, quitItem);
    }

    private void addFunctionBlock(FunctionEntry entry) {
        try {
            FunctionBlock fb = new FunctionBlock(entry.getName(), entry.asHaskellObject(new Context()), parent); // TODO: Once the Env is available, the type should be pulled from the Env here (don't just calculate it over and over). Or just pass the signature String.
            addBlock(fb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addValueBlock() {
        try {
            addBlock(new ValueBlock(parent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDisplayBlock() {
        try {
            addBlock(new DisplayBlock(parent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addBlock(Block block) {
        block.invalidate();
        
        parent.getChildren().add(block);
        Point2D panePos = parent.screenToLocal(this.getX(), this.getY());
        block.relocate(panePos.getX(), panePos.getY());
    }

}
