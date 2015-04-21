package nl.utwente.group10.haskell.catalog;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.CatalogException;

import nl.utwente.group10.haskell.type.TypeClass;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.TreeMultimap;

/**
 * Provides a convenient interface to the XML catalog of available Haskell functions.
 */
public class HaskellFunctionCatalog {
    /** Map from category to entry for use in getCategories and getCategory. */
    private final TreeMultimap<String, FunctionEntry> byCategory;

    /** Map from function name to entry for use in get() and friends. */
    private final Map<String, FunctionEntry> byName;

    /** The default XML data source path. */
    public static final String XML_PATH = "/catalog/functions.xml";

    /** The resource path for the XML Schema definition. */
    public static final String XSD_PATH = "/catalog/functions.xsd";

    /**
     * Construct a HaskellCatalog using the default path.
     * @throws CatalogException when the default XML file can't be found, read, or parsed.
     */
    public HaskellFunctionCatalog() throws CatalogException {
        this(XML_PATH);
    }

    /**
     * Construct a HaskellCatalog from an XML file at the specified path.
     * @param path The path to the XML file containing the collection.
     * @throws CatalogException when the XML file can't be found, read, or parsed.
     */
    public HaskellFunctionCatalog(final String path) throws CatalogException {
        URL functions = HaskellFunctionCatalog.class.getResource(path);
        URL xmlschema = HaskellFunctionCatalog.class.getResource(XSD_PATH);

        this.byName = new HashMap<>();
        this.byCategory = TreeMultimap.create();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            SchemaFactory sFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

            dbFactory.setIgnoringElementContentWhitespace(true);
            dbFactory.setIgnoringComments(true);
            dbFactory.setSchema(sFactory.newSchema(xmlschema));

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(functions.getPath());

            NodeList funcNodes = doc.getElementsByTagName("function");

            for (int j = 0; j < funcNodes.getLength(); j++) {
                Node func = funcNodes.item(j);
                NamedNodeMap funcAttributes = func.getAttributes();

                String funcName = funcAttributes.getNamedItem("name").getTextContent();
                String funcSig = funcAttributes.getNamedItem("signature").getTextContent();
                String funcCat = func.getParentNode().getAttributes().getNamedItem("name").getTextContent();

                FunctionEntry e = new FunctionEntry(funcName, funcCat, funcSig, func.getTextContent());

                this.byName.put(funcName, e);
                this.byCategory.put(funcCat, e);
            }

        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new CatalogException(e);
        }
    }

    /**
     * Get a single Entry by its function name.
     * @param key The name of the Entry.
     * @return The Entry with the specified name.
     * @throws java.util.NoSuchElementException when the function is not defined.
     */
    public final FunctionEntry getEntry(final String key) {
        return this.byName.get(key);
    }

    /**
     * Get a single Entry if it exists, or return the default.
     * @param key The name of the Entry to get,
     * @param defaultValue The default value to return when the Entry does not exist.
     * @return The Entry if it exists, or the specified default.
     */
    public final FunctionEntry getOrDefault(final String key, final FunctionEntry defaultValue) {
        return this.byName.getOrDefault(key, defaultValue);
    }

    /**
     * Get an Optional value with a single Entry if it exists.
     * @param key The name of the Entry.
     * @return An optional that contains the Entry if it exists.
     */
    public final Optional<FunctionEntry> getMaybe(final String key) {
        return Optional.ofNullable(this.getOrDefault(key, null));
    }

    /**
     * Returns a Set of the known categories.
     * @return The Set of known categories.
     */
    public final Set<String> getCategories() {
        return this.byCategory.keySet();
    }

    /**
     * Returns all functions in the given category, or an empty collection.
     * @param name The category name.
     * @return The collection of functions in the given cateagory.
     */
    public final Collection<FunctionEntry> getCategory(String name) {
        return this.byCategory.get(name);
    }

    /**
     * Creates a new environment and adds all entries in this catalog to the environment.
     * @param typeClasses A map of available type classes prior to this catalog.
     * @return The new environment.
     */
    public final Env asEnvironment(final Map<String, TypeClass> typeClasses) {
        final Env env = new Env();

        for (FunctionEntry entry : this.byName.values()) {
            env.getExprTypes().put(entry.getName(), entry.getType(typeClasses));
        }

        return env;
    }
}
