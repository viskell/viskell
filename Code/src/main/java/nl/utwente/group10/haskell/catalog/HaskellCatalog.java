package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.CatalogException;

/**
 * General Haskell catalog that basically is an interface to HaskellClassCatalog and HaskellFunctionCatalog.
 */
public class HaskellCatalog {
    /** Catalog containing the classes. */
    private HaskellClassCatalog classes;

    /** Catalog containing the functions. */
    private HaskellFunctionCatalog functions;

    /**
     * @param classesPath The path where the class XML file is located.
     * @param functionsPath The path where the function XML file is located.
     * @throws CatalogException when one of the XML files can't be found, read or parsed.
     */
    public HaskellCatalog(final String classesPath, final String functionsPath) throws CatalogException {
        if (classesPath == null) {
            this.classes = new HaskellClassCatalog();
        } else {
            this.classes = new HaskellClassCatalog(classesPath);
        }

        if (functionsPath == null) {
            this.functions = new HaskellFunctionCatalog();
        } else {
            this.functions = new HaskellFunctionCatalog(functionsPath);
        }
    }

    /**
     * Constructs a HaskellCatalog from the default file paths.
     * @throws CatalogException when one of the XML files can't be found, read or parsed.
     */
    public HaskellCatalog() throws CatalogException {
        this(null, null);
    }

    public final Env asEnvironment() {
        Env classEnv = classes.asEnvironment(null);
        Env functionEnv = functions.asEnvironment(classEnv.getTypeClasses());
        return Env.join(classEnv, functionEnv);
    }
}
