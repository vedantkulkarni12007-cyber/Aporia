package com.aporia.graph.layout;

/**
 * Encapsulates the calculated layout position and the node's depth in the hierarchy.
 */
public record NodeLayout(Point point, int depth) {
}
