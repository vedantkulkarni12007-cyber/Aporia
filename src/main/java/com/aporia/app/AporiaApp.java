package com.aporia.app;

import com.aporia.graph.Graph;
import com.aporia.model.Node;
import com.aporia.ui.camera.Camera;
import com.aporia.ui.graph.GraphRenderer;
import com.aporia.ui.graph.InteractionHandler;
import com.aporia.ui.state.VisualGraph;
import com.aporia.knowledge.KnowledgeAggregator;
import com.aporia.knowledge.KnowledgeException;
import com.aporia.knowledge.KnowledgeMapper;
import com.aporia.knowledge.KnowledgeResult;
import com.aporia.knowledge.WikidataKnowledgeSource;
import com.aporia.knowledge.WikipediaKnowledgeSource;
import com.aporia.knowledge.http.DefaultHttpTransport;
import com.aporia.knowledge.http.HttpTransport;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AporiaApp extends Application {

    private VisualGraph visualGraph;
    private Camera camera;
    private GraphRenderer renderer;
    private KnowledgeAggregator aggregator;
    
    // Request counter to handle search race conditions
    private int searchRequestCounter = 0;

    @Override
    public void start(Stage primaryStage) {
        // 1. Reduced Motion Preference
        boolean reducedMotion = false;

        // 2. Initialize Core Dependencies
        visualGraph = new VisualGraph();
        camera = new Camera();
        camera.setReducedMotion(reducedMotion);
        
        HttpTransport http = new DefaultHttpTransport();
        aggregator = new KnowledgeAggregator(
            new WikipediaKnowledgeSource(http),
            new WikidataKnowledgeSource(http)
        );

        // 3. Setup Canvas and Renderer
        Canvas canvas = new Canvas(800, 600);
        renderer = new GraphRenderer(canvas, visualGraph, camera);
        renderer.setReducedMotion(reducedMotion);

        // 4. Input Handling for Graph Interaction
        new InteractionHandler(canvas, camera, visualGraph);

        // 5. Setup Minimal Search UI
        TextField searchField = new TextField();
        searchField.setPromptText("Search knowledge...");
        searchField.setStyle("-fx-background-color: #2A2A28; -fx-text-fill: #F0EAD6; -fx-prompt-text-fill: #A69F91; -fx-border-color: #B59E80; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-padding: 5px 10px;");
        
        Button searchBtn = new Button("Explore");
        searchBtn.setStyle("-fx-background-color: #B59E80; -fx-text-fill: #1A1A18; -fx-background-radius: 3px; -fx-padding: 5px 15px; -fx-font-weight: bold; -fx-cursor: hand;");
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #A69F91;");
        
        HBox searchBox = new HBox(10, searchField, searchBtn, statusLabel);
        searchBox.setPadding(new Insets(20));
        searchBox.setPickOnBounds(false); // Let mouse events pass through to canvas
        
        Runnable performSearch = () -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) return;
            
            final int currentRequestId = ++searchRequestCounter;
            statusLabel.setText("Exploring...");
            statusLabel.setStyle("-fx-text-fill: #A69F91;"); // Neutral
            
            // Execute retrieval on background thread to prevent UI freezing
            Thread thread = new Thread(() -> {
                try {
                    KnowledgeResult result = aggregator.search(query);
                    Platform.runLater(() -> {
                        // Only apply if this is still the most recent request
                        if (currentRequestId == searchRequestCounter) {
                            statusLabel.setText("");
                            updateGraph(result);
                        }
                    });
                } catch (KnowledgeException ex) {
                    Platform.runLater(() -> {
                        if (currentRequestId == searchRequestCounter) {
                            statusLabel.setText("Could not retrieve that concept.");
                            statusLabel.setStyle("-fx-text-fill: #E35353;"); // Error color
                            System.err.println("Search failed: " + ex.getMessage());
                        }
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        };

        searchBtn.setOnAction(e -> performSearch.run());
        searchField.setOnAction(e -> performSearch.run());

        // 6. Setup Layout
        StackPane root = new StackPane();
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        
        root.getChildren().addAll(canvas, searchBox);
        StackPane.setAlignment(searchBox, javafx.geometry.Pos.TOP_LEFT);

        // 7. Animation Loop
        AnimationTimer timer = new AnimationTimer() {
            private long lastTime = -1;
            private final long startTime = System.nanoTime();

            @Override
            public void handle(long now) {
                if (lastTime == -1) {
                    lastTime = now;
                    return;
                }
                
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                double elapsedSeconds = (now - startTime) / 1_000_000_000.0;
                
                camera.update(dt);
                renderer.draw(elapsedSeconds);
            }
        };
        timer.start();

        Scene scene = new Scene(root, 800, 600);
        scene.setFill(javafx.scene.paint.Color.web("#1A1A18"));
        primaryStage.setTitle("Aporia");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initial fallback graph for empty state
        Graph initialGraph = new Graph();
        Node initialNode = new Node("welcome", "Aporia Observatory");
        initialGraph.addNode(initialNode);
        visualGraph.initializeFromDomain(initialGraph, initialNode);
    }
    
    private void updateGraph(KnowledgeResult result) {
        Graph newGraph = new Graph();
        KnowledgeMapper.appendToGraph(newGraph, result);
        
        // The primary concept becomes the depth-0 root node
        Node rootNode = newGraph.getNode(result.primaryConcept().id());
        visualGraph.initializeFromDomain(newGraph, rootNode);
        
        // Provide immediate user feedback by selecting the central node
        visualGraph.selectNode(visualGraph.getNodes().stream()
            .filter(vn -> vn.getDomainNode().equals(rootNode))
            .findFirst().orElse(null));
        
        // Recenter camera to the newly layout-ed root node
        camera.reset();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
