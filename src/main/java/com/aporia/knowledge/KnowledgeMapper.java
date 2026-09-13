package com.aporia.knowledge;

import com.aporia.graph.Graph;
import com.aporia.model.Edge;
import com.aporia.model.Node;

/**
 * An anti-corruption layer that converts semantic knowledge concepts into the abstract graph domain.
 */
public class KnowledgeMapper {

    /**
     * Converts a KnowledgeResult into Graph nodes and edges, appending them to the provided graph.
     * 
     * @param graph  The graph domain model to mutate.
     * @param result The semantic knowledge to import.
     */
    public static void appendToGraph(Graph graph, KnowledgeResult result) {
        if (graph == null || result == null) {
            return;
        }

        // 1. Ensure primary concept exists as a Node
        addConceptAsNode(graph, result.primaryConcept());

        // 2. Ensure all related concepts exist as Nodes
        for (KnowledgeConcept concept : result.relatedConcepts()) {
            addConceptAsNode(graph, concept);
        }

        // 3. Construct the edges between the Nodes
        for (KnowledgeRelation relation : result.relations()) {
            Node source = graph.getNode(relation.sourceId());
            Node target = graph.getNode(relation.targetId());

            if (source != null && target != null) {
                // Ensure we don't duplicate edges that already exist
                boolean edgeExists = false;
                for (Edge existing : graph.getOutgoingEdges(source)) {
                    if (existing.target().getId().equals(target.getId()) && existing.relationship().equals(relation.type())) {
                        edgeExists = true;
                        break;
                    }
                }
                
                if (!edgeExists) {
                    graph.addEdge(new Edge(source, target, relation.type()));
                }
            }
        }
    }

    private static void addConceptAsNode(Graph graph, KnowledgeConcept concept) {
        if (!graph.containsNode(concept.id())) {
            // Passing the description to retain knowledge metadata for the UI detail panel
            graph.addNode(new Node(concept.id(), concept.title(), concept.description()));
        }
    }
}
