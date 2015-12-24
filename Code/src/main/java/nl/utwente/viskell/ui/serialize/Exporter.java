package nl.utwente.viskell.ui.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.utwente.viskell.ui.ToplevelPane;

import java.util.Comparator;

/**
 * Convert Viskell programs into JSON text.
 */
public class Exporter {
    private Exporter() {
        // This is a static utility class.
    }

    /**
     * Exports the contents of a CustomUIPane into JSON format.
     *
     * @param pane The pane to export the children of.
     * @return a pretty-printed JSON string.
     */
    public static String export(ToplevelPane pane) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(pane.streamChildren()
                .filter(n -> n instanceof Bundleable)
                .sorted(Comparator.comparing(u -> u.getClass().getName()).thenComparing(Object::hashCode))
                .map(n -> ((Bundleable) n).toBundle())
                .toArray());
    }
}
