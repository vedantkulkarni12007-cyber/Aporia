package com.aporia.knowledge;

import com.aporia.knowledge.http.DefaultHttpTransport;
import org.junit.jupiter.api.Test;

public class VerifyLive {
    @Test
    public void testLive() {
        try {
            DefaultHttpTransport http = new DefaultHttpTransport();
            WikipediaKnowledgeSource wiki = new WikipediaKnowledgeSource(http);
            WikidataKnowledgeSource wikiData = new WikidataKnowledgeSource(http);
            KnowledgeAggregator aggregator = new KnowledgeAggregator(wiki, wikiData);
            
            String[] queries = {"Science", "Astronomy"};
            
            for (String query : queries) {
                System.out.println("\n==========================================");
                System.out.println("Testing query: " + query);
                System.out.println("==========================================");
                
                try {
                    KnowledgeResult result = aggregator.search(query);
                    KnowledgeConcept primary = result.primaryConcept();
                    System.out.println("PRIMARY: " + primary.title() + " (" + primary.id() + ")");
                    System.out.println("DESC: " + primary.description().substring(0, Math.min(100, primary.description().length())) + "...");
                    
                    System.out.println("\n--- RELATIONS ---");
                    for (KnowledgeRelation rel : result.relations()) {
                        System.out.println(rel.sourceId() + " --[" + rel.type() + " (" + rel.category() + ")]--> " + rel.targetId());
                    }
                    
                    System.out.println("\n--- RELATED CONCEPTS ---");
                    for (KnowledgeConcept c : result.relatedConcepts()) {
                        System.out.println("Concept: " + c.id() + " | Title: " + c.title());
                    }
                } catch (Exception e) {
                    System.out.println("FAILED: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
