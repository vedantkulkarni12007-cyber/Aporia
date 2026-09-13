package com.aporia.ui.state;

import com.aporia.model.Node;

/**
 * Presentation-layer state for a Node.
 */
public class VisualNode {
    private final Node domainNode;
    private double x;
    private double y;
    private int depth;
    private boolean selected;

    public VisualNode(Node domainNode, double x, double y, int depth) {
        this.domainNode = domainNode;
        this.x = x;
        this.y = y;
        this.depth = depth;
        this.selected = false;
    }

    public Node getDomainNode() {
        return domainNode;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
