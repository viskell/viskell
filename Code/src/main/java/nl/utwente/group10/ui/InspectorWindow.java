package nl.utwente.group10.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Expression;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.blocks.Block;

import java.util.Optional;

/**
 * This class provides a developer interface to inspect the current state
 * of the Viskell expr tree and give details on the used Haskell source code.
 */
public class InspectorWindow extends BorderPane implements ComponentLoader {
    private ObjectProperty<Optional<Block>> block;
    private Stage stage;
    private CustomUIPane pane;

    @FXML private TreeView<String> tree;
    @FXML private TextArea hs;

    public InspectorWindow(CustomUIPane parentPane) {
        loadFXML("InspectorWindow");
        block = new SimpleObjectProperty<>();
        pane = parentPane;

        stage = new Stage();
        stage.setTitle("Inspect");
        stage.setScene(new Scene(this, 450, 450));

        block.addListener(e -> this.update());
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.hide();
    }

    public Optional<Block> getBlock() {
        return block.get();
    }

    public ObjectProperty<Optional<Block>> blockProperty() {
        return block;
    }

    public void update() {
        this.block.get().ifPresent(block -> {
            Expression expr = block.getExpr();
            String haskell = expr.toHaskell();

            String label = String.format("%s: %s", block.getClass().getSimpleName(), haskell);
            TreeItem<String> root = new TreeItem<>(label);
            root.setExpanded(true);
            walk(root, expr);

            tree.setRoot(root);
            hs.setText(haskell);
        });
    }

    /**
     * Walks the expr tree, walk recursively calls itself on its children.
     */
    private void walk(TreeItem<String> treeItem, Expression expr) {
        String type;

        try {
            type = expr.findType(pane.getEnvInstance()).toHaskellType();
        } catch (HaskellException e) {
            type = "?";
        }

        TreeItem<String> subTree = new TreeItem<>(String.format("%s :: %s", expr, type));
        subTree.setExpanded(true);

        for (Expression child : expr.getChildren()) {
            walk(subTree, child);
        }

        treeItem.getChildren().add(subTree);
    }
}
