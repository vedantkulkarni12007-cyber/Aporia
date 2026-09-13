package com.aporia.model;

import java.util.Objects;

/**
 * Represents a directed relationship between two Nodes in the Aporia graph.
 */
public record Edge(Node source, Node target, String relationship) {

    public Edge {
        if (source == null) {
            throw new IllegalArgumentException("Source node cannot be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target node cannot be null");
        }
        if (relationship == null || relationship.isBlank()) {
            throw new IllegalArgumentException("Relationship cannot be null or blank");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(source, edge.source) &&
               Objects.equals(target, edge.target) &&
               Objects.equals(relationship, edge.relationship);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, relationship);
    }
}
