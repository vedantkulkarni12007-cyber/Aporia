package com.aporia.knowledge;

import com.aporia.knowledge.http.DefaultHttpTransport;

public class VerifyLive {
    public static void main(String[] args) {
        try {
            DefaultHttpTransport http = new DefaultHttpTransport();
            KnowledgeAggregator aggregator = new KnowledgeAggregator(
                new WikipediaKnowledgeSource(http),
                new WikidataKnowledgeSource(http)
            );
            
            String[] queries = {"Astronomy", "Black Hole", "Gravity", "Einstein's theory"};
            
            for (String query : queries) {
                System.out.println("\nTesting query: " + query);
                try {
                    KnowledgeResult result = aggregator.search(query);
                    System.out.println("SUCCESS!");
                    System.out.println("Primary: " + result.primaryConcept().title());
                    System.out.println("Desc: " + result.primaryConcept().description().substring(0, Math.min(50, result.primaryConcept().description().length())) + "...");
                    System.out.println("Relations: " + result.relations().size());
                    System.out.println("Related concepts: " + result.relatedConcepts().size());
                } catch (Exception e) {
                    System.out.println("FAILED: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
