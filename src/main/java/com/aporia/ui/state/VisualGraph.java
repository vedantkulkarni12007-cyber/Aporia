package com.aporia.ui.state;

import com.aporia.graph.Graph;
import com.aporia.graph.layout.NodeLayout;
import com.aporia.graph.layout.RadialLayout;
import com.aporia.model.Edge;
import com.aporia.model.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the presentation-layer graph.
 */
public class VisualGraph {
    private final Map<String, VisualNode> nodes = new HashMap<>();
    private final List<VisualEdge> edges = new ArrayList<>();
    private VisualNode selectedNode = null;
    private VisualNode hoveredNode = null;

    public void initializeFromDomain(Graph graph, Node root) {
        nodes.clear();
        edges.clear();
        selectedNode = null;
        hoveredNode = null;

        Map<Node, NodeLayout> layout = RadialLayout.calculate(graph, root, 150.0);

        // Create visual nodes
        for (Map.Entry<Node, NodeLayout> entry : layout.entrySet()) {
            Node n = entry.getKey();
            NodeLayout nl = entry.getValue();
            nodes.put(n.getId(), new VisualNode(n, nl.point().x(), nl.point().y(), nl.depth()));
        }

        // Create visual edges
        for (Node n : layout.keySet()) {
            VisualNode source = nodes.get(n.getId());
            for (Edge edge : graph.getOutgoingEdges(n)) {
                VisualNode target = nodes.get(edge.target().getId());
                if (target != null) {
                    edges.add(new VisualEdge(edge, source, target));
                }
            }
        }
    }

    public Collection<VisualNode> getNodes() {
        return nodes.values();
    }

    public List<VisualEdge> getEdges() {
        return edges;
    }

    public void selectNode(VisualNode node) {
        if (selectedNode != null) {
            selectedNode.setSelected(false);
        }
        selectedNode = node;
        if (selectedNode != null) {
            selectedNode.setSelected(true);
        }
    }

    public VisualNode getSelectedNode() {
        return selectedNode;
    }

    public void hoverNode(VisualNode node) {
        this.hoveredNode = node;
    }

    public VisualNode getHoveredNode() {
        return hoveredNode;
    }

    public Map<Node, NodeLayout> getLayoutMap() {
        Map<Node, NodeLayout> layoutMap = new HashMap<>();
        for (VisualNode vn : nodes.values()) {
            layoutMap.put(vn.getDomainNode(), new NodeLayout(new com.aporia.graph.layout.Point(vn.getX(), vn.getY()), vn.getDepth()));
        }
        return layoutMap;
    }

    public void expandFromDomain(Graph graph, Map<Node, NodeLayout> layoutMap) {
        // Create new visual nodes for those that don't exist
        for (Map.Entry<Node, NodeLayout> entry : layoutMap.entrySet()) {
            Node n = entry.getKey();
            NodeLayout nl = entry.getValue();
            if (!nodes.containsKey(n.getId())) {
                nodes.put(n.getId(), new VisualNode(n, nl.point().x(), nl.point().y(), nl.depth()));
            }
        }

        // Rebuild edges completely since they are stateless visual lines
        edges.clear();
        for (Node n : layoutMap.keySet()) {
            VisualNode source = nodes.get(n.getId());
            if (source != null) {
                for (Edge edge : graph.getOutgoingEdges(n)) {
                    VisualNode target = nodes.get(edge.target().getId());
                    if (target != null) {
                        edges.add(new VisualEdge(edge, source, target));
                    }
                }
            }
        }
    }
}
