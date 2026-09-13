package com.aporia.ui.state;

import com.aporia.model.Edge;

/**
 * Presentation-layer state for an Edge.
 */
public class VisualEdge {
    private final Edge domainEdge;
    private final VisualNode source;
    private final VisualNode target;

    public VisualEdge(Edge domainEdge, VisualNode source, VisualNode target) {
        this.domainEdge = domainEdge;
        this.source = source;
        this.target = target;
    }

    public Edge getDomainEdge() {
        return domainEdge;
    }

    public VisualNode getSource() {
        return source;
    }

    public VisualNode getTarget() {
        return target;
    }
}
