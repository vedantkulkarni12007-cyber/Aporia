package com.aporia.knowledge;

import com.aporia.graph.Graph;
import com.aporia.model.Node;
import com.aporia.model.Edge;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeMapperTest {

    @Test
    public void testAppendToGraph() throws KnowledgeException {
        KnowledgeSource source = new InMemoryKnowledgeSource();
        KnowledgeResult result = source.searchConcept("astronomy");
        
        Graph graph = new Graph();
        java.util.List<Node> newNodes = KnowledgeMapper.appendToGraph(graph, result);
        
        assertTrue(graph.containsNode("astronomy"));
        assertTrue(graph.containsNode("physics"));
        assertTrue(graph.containsNode("stars"));
        
        Node astronomyNode = graph.getNode("astronomy");
        assertNotNull(astronomyNode);
        assertNotNull(astronomyNode.getDescription());
        
        assertEquals(3, graph.getNodeCount());
        assertEquals(2, graph.getOutgoingEdges(graph.getNode("astronomy")).size());
        
        assertEquals(3, newNodes.size());
    }
    
    @Test
    public void testAppendToGraphPreventsDuplicates() throws KnowledgeException {
        KnowledgeSource source = new InMemoryKnowledgeSource();
        KnowledgeResult result1 = source.searchConcept("astronomy");
        KnowledgeResult result2 = source.searchConcept("physics");
        
        Graph graph = new Graph();
        KnowledgeMapper.appendToGraph(graph, result1);
        KnowledgeMapper.appendToGraph(graph, result2);
        
        assertTrue(graph.containsNode("astronomy"));
        assertTrue(graph.containsNode("physics"));
        assertTrue(graph.containsNode("stars"));
        assertTrue(graph.containsNode("gravity"));
        
        assertEquals(4, graph.getNodeCount());
        
        // Ensure astronomy->physics edge isn't duplicated
        // It is present in result1 (astronomy->physics) and result2 (astronomy->physics)
        assertEquals(2, graph.getOutgoingEdges(graph.getNode("astronomy")).size());
    }

    @Test
    public void testExpansionEdgeIntegrity() {
        Graph graph = new Graph();
        
        // 1. Manually add an existing node representing an explored concept (e.g. Q105)
        Node existingNode = new Node("Q105", "Knowledge System", "A system for knowledge.");
        graph.addNode(existingNode);
        
        // 2. Create an expansion result where the primary concept shares the SAME stable ID (Q105)
        KnowledgeConcept primary = new KnowledgeConcept("Q105", "Knowledge System", "Expanded desc.");
        KnowledgeConcept related = new KnowledgeConcept("Q999", "New Sub-concept", "Desc.");
        
        java.util.List<KnowledgeRelation> relations = java.util.List.of(
            new KnowledgeRelation("Q105", "Q999", "SUBCLASS_OF", KnowledgeRelation.RelationCategory.CONCEPTUAL)
        );
        
        KnowledgeResult result = new KnowledgeResult(primary, java.util.List.of(related), relations);
        
        // 3. Map the expansion result onto the graph
        java.util.List<Node> newNodes = KnowledgeMapper.appendToGraph(graph, result);
        
        // 4. Verify Identity and Edge Integrity
        assertEquals(2, graph.getNodeCount(), "Should not duplicate Q105");
        assertTrue(graph.containsNode("Q105"));
        assertTrue(graph.containsNode("Q999"));
        
        Node q105 = graph.getNode("Q105");
        assertSame(existingNode, q105, "Must reuse the exact existing Node reference");
        
        java.util.List<Edge> edges = graph.getOutgoingEdges(q105);
        assertEquals(1, edges.size(), "Edge must be attached to the existing node");
        assertEquals("Q999", edges.get(0).target().getId());
        
        assertEquals(1, newNodes.size(), "Only Q999 should be returned as a new visual node");
        assertEquals("Q999", newNodes.get(0).getId());
    }

    @Test
    public void testSelfLoopFiltering() {
        Graph graph = new Graph();
        
        KnowledgeConcept india = new KnowledgeConcept("Q668", "India", "Country in South Asia");
        KnowledgeConcept asia = new KnowledgeConcept("Q48", "Asia", "Continent");
        
        java.util.List<KnowledgeRelation> relations = java.util.List.of(
            // Valid conceptual
            new KnowledgeRelation("Q668", "Q48", "CONTINENT", KnowledgeRelation.RelationCategory.CONCEPTUAL),
            // Invalid conceptual self-loop
            new KnowledgeRelation("Q668", "Q668", "SHARES_BORDER_WITH", KnowledgeRelation.RelationCategory.CONCEPTUAL),
            // Invalid contextual self-loop
            new KnowledgeRelation("Q668", "Q668", "INSTANCE_OF", KnowledgeRelation.RelationCategory.CONTEXTUAL)
        );
        
        KnowledgeResult result = new KnowledgeResult(india, java.util.List.of(asia), relations);
        
        // This should not throw an exception!
        java.util.List<Node> newNodes = KnowledgeMapper.appendToGraph(graph, result);
        
        assertEquals(2, graph.getNodeCount(), "Should map India and Asia");
        assertTrue(graph.containsNode("Q668"));
        assertTrue(graph.containsNode("Q48"));
        
        Node indiaNode = graph.getNode("Q668");
        java.util.List<Edge> edges = graph.getOutgoingEdges(indiaNode);
        
        assertEquals(1, edges.size(), "Should only contain the valid conceptual edge to Asia");
        assertEquals("Q48", edges.get(0).target().getId());
        
        // Ensure no contextual self-loop was added
        assertTrue(indiaNode.getContext().isEmpty(), "Context should be empty because the self-loop was ignored");
    }
}
