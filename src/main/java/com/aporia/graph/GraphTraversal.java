package com.aporia.graph;

import com.aporia.model.Edge;
import com.aporia.model.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Provides deterministic breadth-first traversal of the graph.
 */
public class GraphTraversal {

    private record TraversalStep(Node node, int depth) {}

    /**
     * Traverses the graph breadth-first starting from a given node up to a maximum depth.
     *
     * @param graph     The graph to traverse.
     * @param startNode The node to start the traversal from.
     * @param maxDepth  The maximum depth to explore (0 means only the start node).
     * @return A deterministically ordered list of visited nodes.
     */
    public static List<Node> breadthFirst(Graph graph, Node startNode, int maxDepth) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (startNode == null) {
            throw new IllegalArgumentException("Start node cannot be null");
        }
        if (maxDepth < 0) {
            throw new IllegalArgumentException("Max depth cannot be negative");
        }
        if (!graph.containsNode(startNode.getId())) {
            throw new IllegalArgumentException("Start node is not in the graph");
        }

        List<Node> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<TraversalStep> queue = new LinkedList<>();

        queue.add(new TraversalStep(startNode, 0));
        visited.add(startNode.getId());

        while (!queue.isEmpty()) {
            TraversalStep currentStep = queue.poll();
            Node currentNode = currentStep.node();
            int currentDepth = currentStep.depth();

            result.add(currentNode);

            if (currentDepth < maxDepth) {
                for (Edge edge : graph.getOutgoingEdges(currentNode)) {
                    Node targetNode = edge.target();
                    if (!visited.contains(targetNode.getId())) {
                        visited.add(targetNode.getId());
                        queue.add(new TraversalStep(targetNode, currentDepth + 1));
                    }
                }
            }
        }

        return result;
    }
}
