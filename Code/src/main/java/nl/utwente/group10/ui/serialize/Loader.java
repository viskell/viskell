package nl.utwente.group10.ui.serialize;

import nl.utwente.group10.ui.CustomUIPane;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Loader {
    CustomUIPane pane;

    public Loader(CustomUIPane pane) {
        this.pane = pane;
    }

    public void load(String path) throws IOException {
        load(new File(path));
    }

    public void load(File f) throws IOException {
        load(new FileInputStream(f));
    }

    public void load(InputStream stream) throws IOException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(stream);

            NodeList nodes = doc.getDocumentElement().getChildNodes();
            ArrayList<javafx.scene.Node> objs = new ArrayList<>();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                try {
                    // Java reflection trickery follows.
                    Class klass = Class.forName(node.getNodeName());
                    Constructor konstrukt = klass.getDeclaredConstructor(CustomUIPane.class);
                    Object obj = konstrukt.newInstance(pane);

                    assert (obj instanceof Loadable);
                    assert (obj instanceof javafx.scene.Node);

                    objs.add((javafx.scene.Node) obj);
                    // End of trickery. Take a deep breath.
                } catch (ClassCastException | ClassNotFoundException | AssertionError | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    throw new IOException("Program is not valid: " + e);
                }
            }

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                Map<String, String> map = new HashMap<>();
                NamedNodeMap attrs = node.getAttributes();

                for (int j = 0; j < attrs.getLength(); j++) {
                    Node attribute = attrs.item(j);
                    map.put(attribute.getNodeName(), attribute.getNodeValue());
                }

                int id = Integer.valueOf(map.get("id"));

                ((Loadable) objs.get(id)).fromBundle(map);
            }

            pane.getChildren().setAll(objs);
            pane.invalidate();
        } catch (ParserConfigurationException e) {
            // Highly unlikely.
            throw new AssertionError(e);
        } catch (SAXException e) {
            throw new IOException("Couldn't parse saved program: " + e);
        }
    }
}
