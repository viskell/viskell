package nl.utwente.viskell.ui.serialize;

/**
 * Created by andrew on 03/04/16.
 *
 * A class used by Importer and Exported to know what version of the serialized format is being read/written
 *
 * This version number should be increased when breaking changes are made. i.e. when a file can be created
 * by a newer version of the app that cannot be read by a previous version of the app.
 *
 * I suggest we document here the changes made that have caused each new version by adding a version number
 * and explaining in comments why it is needed
 *
 */
public class ViskellFormat {
    public final static String VERSION_NUMBER_LABEL = "ViskellVersion"
    /**
     * Added export capability -> version number 1 (initial version)
     */
    public final static int EXPORT_VERSION_1 = 1;

    /**
     * The current version used in exports
     */
    public final static int EXPORT_VERSION = EXPORT_VERSION_1;

    /**
     * In the future, in case we attempt to support reading older version numbers, we can maintain a list of the
     * versions the current code can read
     */
    public final static int[] SUPPORTED_IMPORT_VERSIONS = {EXPORT_VERSION_1};
}
