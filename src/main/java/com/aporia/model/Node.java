package com.aporia.model;

import java.util.Objects;

/**
 * Represents a knowledge concept in the Aporia graph.
 * Identity is based entirely on the stable ID.
 */
public class Node {
    private final String id;
    private final String label;
    private final String description;

    public Node(String id, String label, String description) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Node ID cannot be null or blank");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Node label cannot be null or blank");
        }
        this.id = id;
        this.label = label;
        this.description = description;
    }

    public Node(String id, String label) {
        this(id, label, null);
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
