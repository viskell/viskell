package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.exceptions.CatalogException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.net.URL;

/**
 * Abstract class for XML-based catalogs.
 */
public abstract class Catalog {
    /**
     * Constructs a new Catalog and loads the given XML file and schema. Parses the XML file by calling the
     * {@code parse} method which is implemented by subclasses.
     * @param XMLPath The path to the XML file.
     * @param XSDPath The path to the XSD file.
     * @throws CatalogException
     */
    protected Catalog(final String XMLPath, final String XSDPath) throws CatalogException {
        URL xmlFile = HaskellCatalog.class.getResource(XMLPath);
        URL schemaFile = HaskellCatalog.class.getResource(XSDPath);

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            SchemaFactory sFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

            dbFactory.setIgnoringElementContentWhitespace(true);
            dbFactory.setIgnoringComments(true);
            dbFactory.setSchema(sFactory.newSchema(schemaFile));

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile.getPath());

            this.parse(doc);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new CatalogException(e);
        }
    }

    /**
     * Parses an XML document and saves the result to the class. This method is called from the constructor.
     * @param doc The document to parse.
     */
    protected abstract void parse(final Document doc);
}
