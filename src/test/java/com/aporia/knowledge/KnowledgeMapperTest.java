package com.aporia.knowledge;

import com.aporia.graph.Graph;
import com.aporia.model.Node;
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
}
