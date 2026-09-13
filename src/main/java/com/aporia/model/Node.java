package com.aporia.model;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a knowledge concept in the Aporia graph.
 * Identity is based entirely on the stable ID.
 */
public class Node {
    private final String id;
    private final String label;
    private final String description;

    private final Map<String, java.util.List<String>> context = new java.util.LinkedHashMap<>();

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

    public void addContext(String key, String value) {
        context.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(value);
    }

    public Map<String, java.util.List<String>> getContext() {
        return java.util.Collections.unmodifiableMap(context);
    }

    public Node copy() {
        Node clone = new Node(this.id, this.label, this.description);
        for (Map.Entry<String, java.util.List<String>> entry : this.context.entrySet()) {
            for (String val : entry.getValue()) {
                clone.addContext(entry.getKey(), val);
            }
        }
        return clone;
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
