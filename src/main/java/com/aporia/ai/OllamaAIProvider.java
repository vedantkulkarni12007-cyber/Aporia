package com.aporia.ai;

import com.aporia.knowledge.KnowledgeConcept;
import com.aporia.knowledge.KnowledgeRelation;
import com.aporia.knowledge.KnowledgeResult;
import com.aporia.knowledge.http.HttpTransport;
import com.aporia.knowledge.json.MiniJson;

import java.util.Map;

/**
 * Local AI Provider implementation for Ollama via its local HTTP REST API.
 */
public class OllamaAIProvider implements LocalAIProvider {

    private final HttpTransport http;
    private final String baseUrl;
    private final String modelName;
    private final int timeoutSeconds;

    public OllamaAIProvider(HttpTransport http, String baseUrl, String modelName, int timeoutSeconds) {
        this.http = http;
        this.baseUrl = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : "http://localhost:11434";
        this.modelName = (modelName != null && !modelName.isBlank()) ? modelName : "llama3.2"; // Sensible default
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : 60; // Ollama generation can be slow
    }

    @Override
    public AIResponse generate(AIRequest request) throws AIException {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new AIException("Invalid AIRequest: Query cannot be empty.");
        }

        String prompt = buildPrompt(request);
        String jsonPayload = buildJsonPayload(prompt);

        try {
            String url = baseUrl.endsWith("/") ? baseUrl + "api/generate" : baseUrl + "/api/generate";
            String responseJson = http.post(url, jsonPayload, timeoutSeconds);
            
            return parseResponse(responseJson);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            throw new AIException("Ollama generation interrupted.", e);
        } catch (Exception e) {
            throw new AIException("Failed to generate response from Ollama: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(AIRequest request) {
        StringBuilder sb = new StringBuilder();
        
        // 1. System/Instruction
        sb.append("You are Aporia's local knowledge assistant.\n");
        sb.append("Use the supplied knowledge context as the primary factual source.\n");
        sb.append("Do not invent relationships or facts that are not supported by the context.\n");
        sb.append("If the context is insufficient, say so.\n\n");
        
        // 2. Query
        sb.append("Query:\n").append(request.query()).append("\n\n");
        
        // 3. Instruction
        if (request.instruction() != null && !request.instruction().isBlank()) {
            sb.append("Instruction:\n").append(request.instruction()).append("\n\n");
        }
        
        // 4. Grounding Context Serialization
        sb.append("Knowledge Context:\n");
        if (request.context() == null) {
            sb.append("(No context provided)\n");
        } else {
            KnowledgeResult ctx = request.context();
            KnowledgeConcept primary = ctx.primaryConcept();
            if (primary != null) {
                sb.append("Primary Concept: ").append(primary.title()).append("\n");
                if (primary.description() != null && !primary.description().isBlank()) {
                    sb.append("Description: ").append(primary.description()).append("\n");
                }
            }
            
            sb.append("\nRelationships:\n");
            for (KnowledgeRelation relation : ctx.relations()) {
                String sourceTitle = getTitle(relation.sourceId(), ctx);
                String targetTitle = getTitle(relation.targetId(), ctx);
                sb.append("- ").append(sourceTitle)
                  .append(" is ").append(relation.type())
                  .append(" ").append(targetTitle).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    private String getTitle(String id, KnowledgeResult ctx) {
        if (ctx.primaryConcept() != null && ctx.primaryConcept().id().equals(id)) {
            return ctx.primaryConcept().title();
        }
        if (ctx.relatedConcepts() != null) {
            for (KnowledgeConcept c : ctx.relatedConcepts()) {
                if (c.id().equals(id)) return c.title();
            }
        }
        return id; // fallback
    }

    private String buildJsonPayload(String prompt) {
        // We manually construct the JSON string to avoid dragging in full JSON serialization libraries
        // purely to write 3 keys. MiniJson is read-only.
        String escapedPrompt = escapeJsonString(prompt);
        return String.format("{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false}", 
                modelName, escapedPrompt);
    }
    
    private String escapeJsonString(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private AIResponse parseResponse(String jsonStr) throws AIException {
        try {
            Object parsed = MiniJson.parse(jsonStr);
            if (!(parsed instanceof Map)) {
                throw new AIException("Malformed JSON response: Expected object.");
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) parsed;
            Object responseObj = map.get("response");
            
            if (responseObj == null) {
                throw new AIException("Missing 'response' field in Ollama output.");
            }
            
            String responseText = responseObj.toString();
            if (responseText.isBlank()) {
                throw new AIException("Ollama returned an empty response.");
            }
            
            return new AIResponse(responseText);
        } catch (Exception e) {
            // Rethrow AIException if we threw it above
            if (e instanceof AIException) throw e;
            throw new AIException("Failed to parse Ollama JSON response: " + e.getMessage(), e);
        }
    }
}
