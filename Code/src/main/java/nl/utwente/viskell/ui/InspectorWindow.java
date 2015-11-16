package nl.utwente.viskell.ui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.ui.components.Block;
import nl.utwente.viskell.ui.serialize.Exporter;

/**
 * This class provides a developer interface to inspect the current state
 * of the Viskell expr tree and give details on the used Haskell source code.
 */
public class InspectorWindow extends BorderPane implements ComponentLoader {
    private Stage stage;
    private CustomUIPane pane;

    @FXML private TreeView<String> tree;
    @FXML private TextArea hs;
    @FXML private TextArea json;

    public InspectorWindow(CustomUIPane parentPane) {
        loadFXML("InspectorWindow");
        pane = parentPane;

        stage = new Stage();
        stage.setTitle("Inspect");
        stage.setScene(new Scene(this, 450, 450));
    }

    public void show() {
        stage.show();
        this.update();
    }

    public void hide() {
        stage.hide();
    }

    public void update() {
        json.setText(Exporter.export(pane));
        TreeItem<String> root = new TreeItem<>("all blocks");
        root.setExpanded(true);

        // show information on all bottom most blocks
        StringBuilder haskell = new StringBuilder();
        for (Node node : this.pane.getChildren()) {
            if (node instanceof Block && ((Block)node).isBottomMost()) {
                Block block = (Block)node;
                Expression expr = block.getFullExpr();
                haskell.append(expr.toHaskell());
                haskell.append("\n\n");
                
                String label = String.format("%s: %s", block.getClass().getSimpleName(), haskell);
                TreeItem<String> part = new TreeItem<>(label);
                part.setExpanded(true);
                this.walk(part, expr);
                root.getChildren().add(part);
            }
        }
         
        tree.setRoot(root);        
        hs.setText(haskell.toString());
    }

    /**
     * Walks the expr tree, walk recursively calls itself on its children.
     */
    private void walk(TreeItem<String> treeItem, Expression expr) {
        String type;

        try {
            type = expr.inferType().prettyPrint();
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
