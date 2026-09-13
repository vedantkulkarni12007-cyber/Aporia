package com.aporia.graph;

import com.aporia.model.Edge;
import com.aporia.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the core knowledge graph in Aporia.
 * Enforces deterministic order using LinkedHashMap.
 */
public class Graph {
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<Node, List<Edge>> outgoingEdges = new LinkedHashMap<>();
    private int edgeCount = 0;

    public void addNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        if (nodes.containsKey(node.getId())) {
            throw new IllegalArgumentException("Graph already contains a node with ID: " + node.getId());
        }
        nodes.put(node.getId(), node);
        outgoingEdges.put(node, new ArrayList<>());
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public boolean containsNode(String id) {
        return nodes.containsKey(id);
    }

    public void addEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Edge cannot be null");
        }
        if (!nodes.containsKey(edge.source().getId())) {
            throw new IllegalArgumentException("Source node is not in the graph");
        }
        if (!nodes.containsKey(edge.target().getId())) {
            throw new IllegalArgumentException("Target node is not in the graph");
        }
        if (edge.source().equals(edge.target())) {
            throw new IllegalArgumentException("Self-loops are not allowed");
        }

        List<Edge> edges = outgoingEdges.get(edge.source());
        if (edges.contains(edge)) {
            throw new IllegalArgumentException("Edge already exists");
        }

        edges.add(edge);
        edgeCount++;
    }

    public List<Edge> getOutgoingEdges(Node node) {
        if (!nodes.containsKey(node.getId())) {
            throw new IllegalArgumentException("Node is not in the graph");
        }
        return Collections.unmodifiableList(outgoingEdges.get(node));
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public void clear() {
        nodes.clear();
        outgoingEdges.clear();
        edgeCount = 0;
    }
}
