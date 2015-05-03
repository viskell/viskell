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
     * Loads the given XML catalog into a document.
     * @param XMLPath The path to the XML file.
     * @param XSDPath The path to the XSD file.
     * @return The document for the XML file.
     * @throws CatalogException
     */
    protected static Document getDocument(final String XMLPath, final String XSDPath) throws CatalogException {
        URL xmlFile = Catalog.class.getResource(XMLPath);
        URL schemaFile = Catalog.class.getResource(XSDPath);

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            SchemaFactory sFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

            dbFactory.setIgnoringElementContentWhitespace(true);
            dbFactory.setIgnoringComments(true);
            dbFactory.setSchema(sFactory.newSchema(schemaFile));

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            return dBuilder.parse(xmlFile.openStream());
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new CatalogException(e);
        }
    }
}
