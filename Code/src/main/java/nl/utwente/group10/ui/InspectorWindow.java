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
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.blocks.Block;

import java.util.Optional;

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
            Expr expr = block.updateExpr();
            String haskell = expr.toHaskell();

            String label = String.format("%s: %s", block.getClass().getSimpleName(), haskell);
            TreeItem<String> root = new TreeItem<>(label);
            root.setExpanded(true);
            walk(root, expr);

            tree.setRoot(root);
            hs.setText(haskell);
        });
    }

    private void walk(TreeItem<String> treeItem, Expr expr) {
        String type;

        // TODO: Would rather *not* call analyze here as analyze is destructive
        try {
            type = expr.analyze(pane.getEnvInstance()).prune().toHaskellType();
        } catch (HaskellException e) {
            type = "?";
        }

        TreeItem<String> subTree = new TreeItem<>(String.format("%s :: %s", expr, type));
        subTree.setExpanded(true);

        for (Expr child : expr.getChildren()) {
            walk(subTree, child);
        }

        treeItem.getChildren().add(subTree);
    }
}
