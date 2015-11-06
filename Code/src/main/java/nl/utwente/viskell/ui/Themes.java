package nl.utwente.viskell.ui;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.prefs.Preferences;

public class Themes {
    private static Preferences prefs = Preferences.userRoot();
    private static final String KEY = "viskell/theme";

    public static final Map<String, String> themes = ImmutableMap.of(
        "Default", "/ui/themes/default.css",
        "Tango", "/ui/themes/tango.css"
    );

    public static String cssPath() {
        return themes.get(name());
    }

    public static String name() {
        return prefs.get(KEY, "Default");
    }

    public static void setTheme(String name) {
        if (themes.containsKey(name)) {
            prefs.put(KEY, name);
        }
    }
}
