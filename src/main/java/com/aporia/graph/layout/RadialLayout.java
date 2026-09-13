package com.aporia.graph.layout;

import com.aporia.graph.Graph;
import com.aporia.model.Edge;
import com.aporia.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A deterministic radial layout algorithm.
 * Places the root node at (0,0) and assigns depth.
 * Distributes subsequent depths in concentric circles.
 */
public class RadialLayout {

    /**
     * Calculates layout coordinates and depth for the given graph starting from a root node.
     *
     * @param graph        The graph to layout.
     * @param root         The center node.
     * @param layerSpacing Distance between concentric depth layers.
     * @return A map of nodes to their calculated layout information.
     */
    public static Map<Node, NodeLayout> calculate(Graph graph, Node root, double layerSpacing) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (root == null) {
            throw new IllegalArgumentException("Root cannot be null");
        }
        if (!graph.containsNode(root.getId())) {
            throw new IllegalArgumentException("Root node is not in the graph");
        }

        Map<Node, NodeLayout> layoutResult = new HashMap<>();
        Map<Integer, List<Node>> depthGroups = new HashMap<>();
        Map<Node, Integer> nodeDepths = new HashMap<>();

        // BFS to determine depths
        Queue<Node> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(root);
        visited.add(root.getId());
        nodeDepths.put(root, 0);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            int depth = nodeDepths.get(current);

            depthGroups.computeIfAbsent(depth, k -> new ArrayList<>()).add(current);

            for (Edge edge : graph.getOutgoingEdges(current)) {
                Node target = edge.target();
                if (!visited.contains(target.getId())) {
                    visited.add(target.getId());
                    nodeDepths.put(target, depth + 1);
                    queue.add(target);
                }
            }
        }

        // Layout
        for (Map.Entry<Integer, List<Node>> entry : depthGroups.entrySet()) {
            int depth = entry.getKey();
            List<Node> nodesAtDepth = entry.getValue();

            if (depth == 0) {
                // Root is at center
                layoutResult.put(nodesAtDepth.get(0), new NodeLayout(new Point(0, 0), depth));
                continue;
            }

            double radius = depth * layerSpacing;
            int count = nodesAtDepth.size();
            double angleStep = (2 * Math.PI) / count;

            for (int i = 0; i < count; i++) {
                Node n = nodesAtDepth.get(i);
                double angle = i * angleStep;
                double x = radius * Math.cos(angle);
                double y = radius * Math.sin(angle);
                layoutResult.put(n, new NodeLayout(new Point(x, y), depth));
            }
        }

        return layoutResult;
    }
}
