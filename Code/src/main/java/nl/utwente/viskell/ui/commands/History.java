package nl.utwente.viskell.ui.commands;

import com.google.common.collect.Iterators;

import java.util.ArrayDeque;
import java.util.Iterator;

public class History implements Iterable<Command> {
    private ArrayDeque<Command> undo;
    private ArrayDeque<Command> redo;

    public History() {
        undo = new ArrayDeque<>();
        redo = new ArrayDeque<>();
    }

    public void add(Command command) {
        undo.push(command);
        redo.clear();
    }

    public void undo() {
        if (!undo.isEmpty()) {
            Command command = undo.pop();
            command.undo();
            redo.push(command);
        }
    }

    public void redo() {
        if (!redo.isEmpty()) {
            Command command = redo.pop();
            command.redo();
            undo.push(command);
        }
    }

    public Iterator<Command> iterator() {
        return Iterators.concat(undo.iterator(), redo.iterator());
    }
}
