package nl.utwente.group10.haskell.catalog;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeClass;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.*;

/**
 * Haskell catalog containing available type classes and functions.
 */
public class HaskellCatalog extends Catalog {
    private Map<String, ClassEntry> classes;

    private Map<String, FunctionEntry> functions;

    private Multimap<String, FunctionEntry> categories;

    /** Default path to the XML file. */
    public static final String XML_PATH = "/catalog/catalog.xml";

    /** Default path to the XSD file. */
    public static final String XSD_PATH = "/catalog/catalog.xsd";

    /**
     * Constructs a Haskell catalog using the given file location.
     * @param path The path to the catalog XML file.
     * @throws CatalogException
     */
    public HaskellCatalog(final String path) throws CatalogException {
        this.classes = new HashMap<>();
        this.functions = new HashMap<>();
        this.categories = HashMultimap.create();

        Document doc = Catalog.getDocument(path, HaskellCatalog.XSD_PATH);

        NodeList classNodes = doc.getElementsByTagName("class");
        NodeList functionNodes = doc.getElementsByTagName("function");

        Set<ClassEntry> classes = this.parseClasses(classNodes);
        Set<FunctionEntry> functions = this.parseFunctions(functionNodes);

        this.setClasses(classes);
        this.setFunctions(functions);
    }

    /**
     * Constructs a Haskell catalog using the default file location.
     * @throws CatalogException
     */
    public HaskellCatalog() throws CatalogException {
        this(HaskellCatalog.XML_PATH);
    }

    /**
     * @return The set of category names.
     */
    public final Set<String> getCategories() {
        return this.categories.keySet();
    }

    /**
     * @param key The name of the category.
     * @return A set of the entries in the given category.
     */
    public final Collection<FunctionEntry> getCategory(final String key) {
        return this.categories.get(key);
    }

    /**
     * @return A new environment based on the entries of this catalog.
     */
    public final Env asEnvironment() {
        Map<String, TypeClass> classes = new HashMap<>();
        Map<String, Type> functions = new HashMap<>();
        Context ctx = new Context();

        // Build type class map
        for (ClassEntry entry : this.classes.values()) {
            classes.put(entry.getName(), entry.asHaskellObject(ctx));
        }

        // Add type classes to context
        ctx.typeClasses = classes;

        // Build function type map
        for (FunctionEntry entry : this.functions.values()) {
            functions.put(entry.getName(), entry.asHaskellObject(ctx));
        }

        return new Env(functions, classes.values());
    }

    /**
     * Parses a list of class nodes into ClassEntry objects.
     * @param nodes The nodes to parse.
     * @return A set of ClassEntry objects for the given nodes.
     */
    protected final Set<ClassEntry> parseClasses(NodeList nodes) {
        Set<ClassEntry> entries = new HashSet<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();

            String name = attributes.getNamedItem("name").getTextContent();
            Set<String> instances = this.parseInstances(node.getChildNodes());

            entries.add(new ClassEntry(name, instances));
        }

        return entries;
    }

    /**
     * Parses a list of instance nodes for ClassEntry objects.
     * @param nodes The nodes to parse.
     * @return A set of String objects for the given nodes.
     */
    protected final Set<String> parseInstances(NodeList nodes) {
        Set<String> instances = new HashSet<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();

            instances.add(attributes.getNamedItem("name").getTextContent());
        }

        return instances;
    }
    
    /**
     * Parses a list of function nodes into FunctionEntry objects.
     * @param nodes The nodes to parse.
     * @return A set of FunctionEntry objects for the given nodes.
     */
    protected final Set<FunctionEntry> parseFunctions(NodeList nodes) {
        Set<FunctionEntry> entries = new HashSet<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();

            String name = attributes.getNamedItem("name").getTextContent();
            String signature = attributes.getNamedItem("signature").getTextContent();
            String category = node.getParentNode().getAttributes().getNamedItem("name").getTextContent();
            String documentation = node.getTextContent();

            entries.add(new FunctionEntry(name, category, signature, documentation));
        }

        return entries;
    }

    /**
     * Sets the type classes for this catalog. Clears out any existing information.
     * @param entries The entries to set.
     */
    protected final void setClasses(Set<ClassEntry> entries) {
        this.classes.clear();

        for (ClassEntry entry : entries) {
            this.classes.put(entry.getName(), entry);
        }
    }

    /**
     * Sets the functions and categories for this catalog. Clears out any existing information.
     * @param entries The entries to set.
     */
    protected final void setFunctions(Set<FunctionEntry> entries) {
        this.functions.clear();
        this.categories.clear();

        for (FunctionEntry entry : entries) {
            this.functions.put(entry.getName(), entry);
            this.categories.put(entry.getCategory(), entry);
        }
    }
}
