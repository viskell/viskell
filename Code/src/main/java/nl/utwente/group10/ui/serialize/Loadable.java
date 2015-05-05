package nl.utwente.group10.ui.serialize;

import java.util.Map;

public interface Loadable {
    void fromBundle(Map<String, String> bundle) throws IllegalArgumentException;
    Map<String, String> toBundle() throws IllegalStateException;
}
