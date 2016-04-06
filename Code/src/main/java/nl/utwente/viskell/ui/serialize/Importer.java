package nl.utwente.viskell.ui.serialize;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;

public class Importer {
    private Importer() {
        // This is a static utility class.
    }

    /**
     * Imports the contents of a file in JSON format into a ToplevelPane
     *
     * @param is Input stream of JSON description of blocks to read
     * @return an array of blocks read from the file
     */
    public static Map<String, Object> readLayers(InputStream is) {
        Reader inputStreamReader = new InputStreamReader(is);
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(inputStreamReader, collectionType);
    }
}
