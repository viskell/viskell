package nl.utwente.group10.ui.serialize;

import javafx.scene.Node;
import nl.utwente.group10.ui.CustomUIPane;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class Exporter {
    CustomUIPane pane;

    public Exporter(CustomUIPane pane) {
        this.pane = pane;
    }

    public void export(String path) throws IOException {
        export(new File(path));
    }

    public void export(File f) throws IOException {
        export(new FileOutputStream(f));
    }

    public void export(OutputStream stream) {
        export(new StreamResult(stream));
    }

    public void export(StreamResult result) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Configure transformer
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            // Root element
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Program");
            doc.appendChild(rootElement);

            // Blocks and connections
            for (Node node : pane.getChildren()) {
                if (node instanceof Loadable) {
                    Loadable loadable = (Loadable) node;
                    Element element = doc.createElement(node.getClass().getName());
                    Map<String, String> bundle = loadable.toBundle();

                    for (String key : bundle.keySet()) {
                        element.setAttribute(key, bundle.get(key));
                    }

                    rootElement.appendChild(element);
                }
            }

            transformer.transform(new DOMSource(doc), result);
        } catch (ParserConfigurationException | TransformerException e) {
            // Highly unlikely.
            throw new AssertionError(e);
        }
    }
}
