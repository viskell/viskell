package nl.utwente.group10.ui.commands;

import javafx.geometry.Point2D;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.blocks.Block;

public class MoveCommand extends Command {
    private Block block;
    private Point2D oldPos;
    private Point2D newPos;

    public MoveCommand(CustomUIPane pane, Block block, Point2D oldPos, Point2D newPos) {
        super(pane);
        this.block = block;
        this.oldPos = oldPos;
        this.newPos = newPos;
    }

    @Override
    public void redo() {
        block.setLayoutX(newPos.getX());
        block.setLayoutY(newPos.getY());
    }

    @Override
    public void undo() {
        block.setLayoutX(oldPos.getX());
        block.setLayoutY(oldPos.getY());
    }

    @Override
    public String toString() {
        return String.format("Move block %s from (%.0f,%.0f) to (%.0f,%.0f)", block, oldPos.getX(), oldPos.getY(), newPos.getX(), newPos.getY());
    }
}
