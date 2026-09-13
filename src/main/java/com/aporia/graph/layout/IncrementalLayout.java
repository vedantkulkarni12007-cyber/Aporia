package com.aporia.graph.layout;

import com.aporia.model.Node;

import java.util.List;
import java.util.Map;

/**
 * Places newly discovered nodes radially around an explored origin node,
 * facing outward to minimize overlap with the existing graph structure.
 */
public class IncrementalLayout {

    public static void expand(Map<Node, NodeLayout> currentLayout, Node originNode, List<Node> newNodes, double spacing) {
        if (newNodes.isEmpty()) {
            return;
        }

        NodeLayout originLayout = currentLayout.get(originNode);
        if (originLayout == null) {
            throw new IllegalArgumentException("Origin node not found in current layout");
        }

        double cx = originLayout.point().x();
        double cy = originLayout.point().y();
        int originDepth = originLayout.depth();

        double baseAngle = Math.atan2(cy, cx);
        boolean isRoot = (Math.abs(cx) < 0.001 && Math.abs(cy) < 0.001);
        
        int count = newNodes.size();
        
        if (isRoot) {
            double angleStep = (2 * Math.PI) / count;
            for (int i = 0; i < count; i++) {
                double angle = i * angleStep;
                double x = cx + spacing * Math.cos(angle);
                double y = cy + spacing * Math.sin(angle);
                currentLayout.put(newNodes.get(i), new NodeLayout(new Point(x, y), originDepth + 1));
            }
        } else {
            // Fan out over 180 degrees facing away from the center
            double arcSpread = Math.PI;
            double startAngle = baseAngle - (arcSpread / 2);
            double angleStep = arcSpread / (count + 1);
            
            for (int i = 0; i < count; i++) {
                double angle = startAngle + ((i + 1) * angleStep);
                double x = cx + spacing * Math.cos(angle);
                double y = cy + spacing * Math.sin(angle);
                currentLayout.put(newNodes.get(i), new NodeLayout(new Point(x, y), originDepth + 1));
            }
        }
    }
}
