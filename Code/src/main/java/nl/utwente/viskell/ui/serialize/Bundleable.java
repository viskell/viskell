package nl.utwente.viskell.ui.serialize;

import java.util.Map;

/**
 * Interface for things that can be turned into bundles (maps of strings to
 * objects), which can in turn be converted into JSON.
 */
public interface Bundleable {
    /**
     * Serialization function.
     *
     * @return a map that describes the bundleable object.
     */
    Map<String, Object> toBundle();
}
