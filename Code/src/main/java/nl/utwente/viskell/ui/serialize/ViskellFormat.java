package nl.utwente.viskell.ui.serialize;

import java.util.Collections;
import java.util.List;

/**
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
    /**
     * This is the name given to the version number in the serialized file and should never be changed
     */
    public final static String VERSION_NUMBER_LABEL = "ViskellVersion";

    /*************************************** EXPORT VERSIONS **********************************/
    /**
     * Added export capability -> version number 1 (initial version)
     */
    public final static int EXPORT_VERSION_1 = 1;

    /**
     * The current version used in exports
     */
    public final static int EXPORT_VERSION = EXPORT_VERSION_1;


    /*************************************** IMPORT VERSIONS **********************************/
    /**
     * In the future, in case we attempt to support reading older version numbers, we can maintain a list of the
     * versions the current code can read
     */
    public final static List<Integer> SUPPORTED_IMPORT_VERSIONS = Collections.singletonList(EXPORT_VERSION_1);
}
