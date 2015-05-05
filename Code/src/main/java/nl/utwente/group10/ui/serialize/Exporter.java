package nl.utwente.group10.ui.serialize;

import javafx.scene.Node;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.FunctionBlock;
import nl.utwente.group10.ui.components.lines.Connection;
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
import java.util.Date;
import java.util.List;

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

            // Top-level tree
            Element metaElement = doc.createElement("Meta");
            Element blocksElement = doc.createElement("Blocks");
            Element connectionsElement = doc.createElement("Connections");
            rootElement.appendChild(metaElement);
            rootElement.appendChild(blocksElement);
            rootElement.appendChild(connectionsElement);

            // Meta
            Element datetime = doc.createElement("Date");
            datetime.appendChild(doc.createTextNode(new Date().toString()));
            metaElement.appendChild(datetime);

            Element user = doc.createElement("User");
            user.appendChild(doc.createTextNode(System.getProperty("user.name")));
            metaElement.appendChild(user);

            Element version = doc.createElement("Version");
            String ver = this.getClass().getPackage().getImplementationVersion();
            if (ver == null) ver = "Unknown";
            version.appendChild(doc.createTextNode(ver));
            metaElement.appendChild(version);

            // Blocks and connections
            List<Node> children = pane.getChildren();
            for (int i = 0; i < children.size(); i++) {
                Node node = children.get(i);

                if (node instanceof Block) {
                    Block block = (Block) node;

                    Element blockElement = doc.createElement(block.getClass().getSimpleName());
                    blockElement.setAttribute("x", String.valueOf(block.getLayoutX()));
                    blockElement.setAttribute("y", String.valueOf(block.getLayoutY()));
                    blockElement.setAttribute("id", String.valueOf(i));

                    if (node instanceof FunctionBlock) {
                        FunctionBlock fblock = (FunctionBlock) block;

                        blockElement.setAttribute("name", fblock.getName());
                    }

                    blocksElement.appendChild(blockElement);
                } else if (node instanceof Connection) {
                    Connection conn = (Connection) node;

                    if (conn.getInputAnchor().isPresent() && conn.getOutputAnchor().isPresent()) {
                        Block from = conn.getOutputBlock().get();
                        Block to = conn.getInputBlock().get();

                        Element connElement = doc.createElement("Connection");
                        connElement.setAttribute("from", String.valueOf(children.indexOf(from)));
                        connElement.setAttribute("to", String.valueOf(children.indexOf(to)));
                        connElement.setAttribute("id", String.valueOf(i));

                        if (to instanceof FunctionBlock) {
                            FunctionBlock fto = (FunctionBlock) to;
                            ConnectionAnchor anchor = conn.getInputAnchor().get();
                            connElement.setAttribute("argument", String.valueOf(fto.getArgumentIndex(anchor)));
                        }

                        connectionsElement.appendChild(connElement);
                    }
                }
            }

            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException e) {
            // Highly unlikely.
            e.printStackTrace();
        }
    }
}
