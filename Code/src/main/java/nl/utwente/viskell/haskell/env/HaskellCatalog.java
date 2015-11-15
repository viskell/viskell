package nl.utwente.viskell.haskell.env;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.haskell.typeparser.TypeBuilder;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Haskell catalog containing available type classes and functions.
 */
public class HaskellCatalog {
    private Map<String, TypeClass> classes;

    private Map<String, CatalogFunction> functions;

    private Multimap<String, CatalogFunction> categories;

    /** Default path to the XML file. */
    public static final String XML_PATH = "/catalog/catalog.xml";

    /** Default path to the XSD file. */
    public static final String XSD_PATH = "/catalog/catalog.xsd";

    /**
     * Constructs a Haskell catalog using the given file location.
     * @param path The path to the catalog XML file.
     */
    public HaskellCatalog(final String path) {
        this.functions = new HashMap<>();
        this.categories = HashMultimap.create();

        Document doc = getDocument(path, HaskellCatalog.XSD_PATH);

        NodeList classNodes = doc.getElementsByTagName("class");
        NodeList functionNodes = doc.getElementsByTagName("function");

        this.classes = this.parseClasses(classNodes);

        Set<CatalogFunction> entries = this.parseFunctions(functionNodes, this.classes);
      
        for (CatalogFunction entry : entries) {
            this.functions.put(entry.getName(), entry);
            this.categories.put(entry.getCategory(), entry);
        }
    }

    /**
     * Constructs a Haskell catalog using the default file location.
     */
    public HaskellCatalog() {
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
    public final Collection<CatalogFunction> getCategory(final String key) {
        return this.categories.get(key);
    }

    /**
     * @return A set of functions that match the given predicate.
     */
    public final Collection<CatalogFunction> getByPredicate(final Predicate<CatalogFunction> predicate) {
        return functions.values().stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * @return A set of functions with names beginning with the given prefix.
     */
    public final Collection<CatalogFunction> getByPrefix(final String prefix) {
        return getByPredicate(fn -> fn.getName().startsWith(prefix));
    }

    /**
     * @return A set of functions that match the given type.
     */
    public final Collection<CatalogFunction> getByType(final Type type) {
        return getByPredicate(fn -> {
            try {
                TypeChecker.unify("catalog query", fn.getFreshSignature(), type.getFresh());
            } catch (HaskellTypeError e) {
                return false;
            }

            return true;
        });
    }

    /**
     * @return The number of functions in the catalog.
     */
    public int size() {
        return functions.size();
    }

    /**
     * @return A new environment based on the entries of this catalog.
     */
    public final Environment asEnvironment() {
        return new Environment(new HashMap<>(this.functions), new HashMap<>(this.classes));
    }

    /**
     * Parses a list of class nodes into ClassEntry objects.
     * @param nodes The nodes to parse.
     * @return A set of ClassEntry objects for the given nodes.
     */
    protected final Map<String, TypeClass> parseClasses(NodeList nodes) {
        Map<String, TypeClass> entries = new HashMap<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            String name = node.getAttributes().getNamedItem("name").getTextContent();
            TypeClass tc = new TypeClass(name);
            TypeBuilder builder = new TypeBuilder(entries);
            
            NodeList inodes = node.getChildNodes();
            for (int j = 0; j < inodes.getLength(); j++) {
                Node inode = inodes.item(j);
                NamedNodeMap attrs = inode.getAttributes();
                String inst = attrs.getNamedItem("name").getTextContent();
                if ("instance".equals(inode.getNodeName())) {
                    String constrArgs = attrs.getNamedItem("constrainedArgs").getTextContent();
                    Type t = builder.build(inst);
                    if (t instanceof TypeCon) {
                        tc.addInstance((TypeCon) t, Integer.parseInt(constrArgs));
                    }
                } else if ("superClass".equals(inode.getNodeName())) {
                    TypeClass sc = entries.get(inst);
                    if (sc == null) {
                       throw new RuntimeException("Can't resolve superclass " + inst + " of " + name);
                    } else {
                        tc.addSuperClass(sc);
                    }
                }
            }
            
            entries.put(name,tc);
        }

        return entries;
    }
    
    /**
     * Parses a list of function nodes into FunctionEntry objects.
     * @param nodes The nodes to parse.
     * @return A set of FunctionEntry objects for the given nodes.
     */
    protected final Set<CatalogFunction> parseFunctions(NodeList nodes, Map<String, TypeClass> typeClasses) {
        Set<CatalogFunction> entries = new HashSet<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();

            String name = attributes.getNamedItem("name").getTextContent();
            String signature = attributes.getNamedItem("signature").getTextContent();
            Boolean isConstructor = attributes.getNamedItem("isConstructor") != null;
            String category = node.getParentNode().getAttributes().getNamedItem("name").getTextContent();
            String documentation = node.getTextContent();

            TypeBuilder builder = new TypeBuilder(typeClasses);
            Type tsig = builder.build(signature);
            entries.add(new CatalogFunction(name, category, tsig, documentation, isConstructor));
        }

        return entries;
    }

    /**
     * Loads the given XML catalog into a document.
     * @param XMLPath The path to the XML file.
     * @param XSDPath The path to the XSD file.
     * @return The document for the XML file.
     */
    protected static Document getDocument(final String XMLPath, final String XSDPath) {
        URL xmlFile = HaskellCatalog.class.getResource(XMLPath);
        URL schemaFile = HaskellCatalog.class.getResource(XSDPath);

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            SchemaFactory sFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

            dbFactory.setIgnoringElementContentWhitespace(true);
            dbFactory.setIgnoringComments(true);
            dbFactory.setSchema(sFactory.newSchema(schemaFile));

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            return dBuilder.parse(xmlFile.openStream());
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException("could not read or parse catalog file", e);
        }
    }
    
}
