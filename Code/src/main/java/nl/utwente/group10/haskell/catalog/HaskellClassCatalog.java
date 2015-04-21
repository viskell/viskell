package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import nl.utwente.group10.haskell.type.TypeClass;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Provides a convenient interface to the XML catalog of available Haskell classes.
 */
public class HaskellClassCatalog {
    /** Map from function name to entry for use in get() and friends. */
    private final Map<String, ClassEntry> byName;

    /** The default XML data source path. */
    public static final String XML_PATH = "/catalog/classes.xml";

    /** The resource path for the XML Schema definition. */
    public static final String XSD_PATH = "/catalog/classes.xsd";

    /**
     * Construct a HaskellCatalog using the default path.
     * @throws CatalogException when the default XML file can't be found, read, or parsed.
     */
    public HaskellClassCatalog() throws CatalogException {
        this(XML_PATH);
    }

    /**
     * Construct a HaskellCatalog from an XML file at the specified path.
     * @param path The path to the XML file containing the collection.
     * @throws CatalogException when the XML file can't be found, read, or parsed.
     */
    public HaskellClassCatalog(final String path) throws CatalogException {
        URL classes = HaskellClassCatalog.class.getResource(path);
        URL xmlschema = HaskellClassCatalog.class.getResource(XSD_PATH);

        this.byName = new HashMap<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            SchemaFactory sFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

            dbFactory.setIgnoringElementContentWhitespace(true);
            dbFactory.setIgnoringComments(true);
            dbFactory.setSchema(sFactory.newSchema(xmlschema));

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(classes.getPath());

            NodeList classNodes = doc.getElementsByTagName("class");

            for (int j = 0; j < classNodes.getLength(); j++) {
                Node cls = classNodes.item(j);
                NamedNodeMap funcAttributes = cls.getAttributes();

                String clsName = funcAttributes.getNamedItem("name").getTextContent();
                NodeList instanceNodes = cls.getChildNodes();

                Set<String> clsInstances = new HashSet<>();

                for (int i = 0; i < instanceNodes.getLength(); i++) {
                    Node instance = instanceNodes.item(i);
                    clsInstances.add(instance.getAttributes().getNamedItem("name").getTextContent());
                }

                ClassEntry e = new ClassEntry(clsName, clsInstances);

                this.byName.put(clsName, e);
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
    public final ClassEntry getEntry(final String key) {
        return this.byName.get(key);
    }

    /**
     * Get a single Entry if it exists, or return the default.
     * @param key The name of the Entry to get,
     * @param defaultValue The default value to return when the Entry does not exist.
     * @return The Entry if it exists, or the specified default.
     */
    public final ClassEntry getOrDefault(final String key, final ClassEntry defaultValue) {
        return this.byName.getOrDefault(key, defaultValue);
    }

    /**
     * Get an Optional value with a single Entry if it exists.
     * @param key The name of the Entry.
     * @return An optional that contains the Entry if it exists.
     */
    public final Optional<ClassEntry> getMaybe(final String key) {
        return Optional.ofNullable(this.getOrDefault(key, null));
    }

    /**
     * Creates a new environment and adds all entries in this catalog to the environment.
     * @param typeClasses A map of available type classes prior to this catalog.
     * @return The new environment.
     */
    public final Env asEnvironment(final Map<String, TypeClass> typeClasses) {
        final Env env = new Env();

        for (ClassEntry entry : this.byName.values()) {
            env.addTypeClass(entry.getTypeClass(typeClasses));
        }

        return env;
    }
}
