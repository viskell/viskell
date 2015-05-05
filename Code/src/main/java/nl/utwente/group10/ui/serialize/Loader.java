package nl.utwente.group10.ui.serialize;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.utwente.group10.ui.CustomUIPane;

import java.io.File;
import java.io.IOException;

public class Loader {
    CustomUIPane pane;

    public Loader(CustomUIPane pane) {
        this.pane = pane;
    }

    public void load(File f) throws IOException {
        System.out.println(Files.toString(f, Charsets.UTF_8));
    }
}
