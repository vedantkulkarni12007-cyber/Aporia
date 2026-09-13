package com.aporia.knowledge;

import com.aporia.graph.Graph;
import com.aporia.model.Edge;
import com.aporia.model.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * An anti-corruption layer that converts semantic knowledge concepts into the abstract graph domain.
 */
public class KnowledgeMapper {

    /**
     * Converts a KnowledgeResult into Graph nodes and edges, appending them to the provided graph.
     * 
     * @param graph  The graph domain model to mutate.
     * @param result The semantic knowledge to import.
     * @return A list of newly added Nodes that were not previously in the graph.
     */
    public static List<Node> appendToGraph(Graph graph, KnowledgeResult result) {
        List<Node> newNodes = new ArrayList<>();
        if (graph == null || result == null) {
            return newNodes;
        }

        // 1. Ensure primary concept exists as a Node
        Node addedPrimary = addConceptAsNode(graph, result.primaryConcept());
        if (addedPrimary != null) newNodes.add(addedPrimary);
        
        List<KnowledgeRelation> conceptualRelations = new ArrayList<>();
        for (KnowledgeRelation r : result.relations()) {
            if (r.category() != KnowledgeRelation.RelationCategory.CONTEXTUAL) {
                conceptualRelations.add(r);
            }
        }

        // 2. Ensure all related concepts exist as Nodes (only if they have a conceptual relation)
        for (KnowledgeConcept concept : result.relatedConcepts()) {
            boolean hasConceptualEdge = false;
            for (KnowledgeRelation r : conceptualRelations) {
                if (r.targetId().equals(concept.id()) || r.sourceId().equals(concept.id())) {
                    hasConceptualEdge = true;
                    break;
                }
            }
            if (hasConceptualEdge) {
                Node addedRelated = addConceptAsNode(graph, concept);
                if (addedRelated != null) newNodes.add(addedRelated);
            }
        }

        // 3. Construct the edges between the Nodes (Conceptual only)
        for (KnowledgeRelation relation : conceptualRelations) {
            if (relation.sourceId().equals(relation.targetId())) {
                continue; // Phase 6.4: ignore self-loops entirely
            }
            
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
        
        // 4. Map CONTEXTUAL relationships into node context
        for (KnowledgeRelation r : result.relations()) {
            if (r.category() == KnowledgeRelation.RelationCategory.CONTEXTUAL) {
                if (r.sourceId().equals(r.targetId())) {
                    continue; // Phase 6.4: safely ignore useless contextual self-loops
                }
                
                Node source = graph.getNode(r.sourceId());
                if (source != null) {
                    // Resolve target ID to a human-readable label
                    String targetLabel = r.targetId();
                    for (KnowledgeConcept c : result.relatedConcepts()) {
                        if (c.id().equals(r.targetId())) {
                            targetLabel = c.title();
                            break;
                        }
                    }
                    source.addContext(r.type(), targetLabel);
                }
            }
        }
        
        return newNodes;
    }

    private static Node addConceptAsNode(Graph graph, KnowledgeConcept concept) {
        if (!graph.containsNode(concept.id())) {
            Node n = new Node(concept.id(), concept.title(), concept.description());
            graph.addNode(n);
            return n;
        }
        return null;
    }
}
