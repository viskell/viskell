package nl.utwente.viskell.model.graphviz;

import nl.utwente.viskell.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphToGraphviz {
    private GraphToGraphviz() {
        /* Utility class, disallow construction. */
    }

    private static void convertBox(Set<Box> boxes, List<Wire> wires, StringBuilder sb, Box box) {
        if (boxes.add(box)) {
            sb.append("  subgraph cluster_" + box.hashCode() + " {\n");
            sb.append("    style=filled;\n");
            sb.append("    node [style=filled,color=white];\n");
            sb.append("    color=lightgrey;\n");
            sb.append("    label=\"" + box.toString() + "\";\n");
            sb.append("\n");

            for (InputPort in : box.getInputs()) {
                sb.append("    in_" + in.hashCode() + " -> \n");
            }

            for (OutputPort out : box.getOutputs()) {
                sb.append("    out_" + out.hashCode() + ";\n");
            }

            sb.append("  }\n");

            for (InputPort in : box.getInputs()) {
                in.getOppositePort().ifPresent(out -> {
                    sb.append("  out_" + out.hashCode() + " -> in_" + in.hashCode() + ";\n");
                });
            }

            sb.append("\n");

            for (InputPort next : box.getInputs()) {
                next.getOppositePort().ifPresent(sourcePort -> {
                    if (sourcePort instanceof OutputPort) {
                        convertBox(boxes, wires, sb, ((OutputPort)sourcePort).getBox());
                    }
                });
            }
        }
    }

    public static String convert(Box box) {
        StringBuilder sb = new StringBuilder();
        Set<Box> existing = new HashSet<>();
        List<Wire> wires = new ArrayList<>();

        sb.append("digraph G {\n");
        sb.append("  rankdir=\"LR\";\n");
        sb.append("  node[shape=record];\n");
        convertBox(existing, wires, sb, box);

        sb.append("}\n");

        return sb.toString();
    }
}
