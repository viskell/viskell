package nl.utwente.group10.ui.commands;

import nl.utwente.group10.ui.CustomUIPane;

public abstract class Command {
    CustomUIPane pane;

    public Command(CustomUIPane pane) {
        this.pane = pane;
    }

    public abstract void redo();
    public abstract void undo();

    public abstract String toString();
}
