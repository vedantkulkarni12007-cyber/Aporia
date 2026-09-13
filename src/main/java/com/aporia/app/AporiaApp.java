package com.aporia.app;

import com.aporia.graph.Graph;
import com.aporia.graph.layout.IncrementalLayout;
import com.aporia.graph.layout.NodeLayout;
import com.aporia.model.Node;
import com.aporia.ui.camera.Camera;
import com.aporia.ui.graph.GraphRenderer;
import com.aporia.ui.graph.InteractionHandler;
import com.aporia.ui.state.VisualGraph;
import com.aporia.ui.state.VisualNode;
import com.aporia.knowledge.KnowledgeAggregator;
import com.aporia.knowledge.KnowledgeException;
import com.aporia.knowledge.KnowledgeMapper;
import com.aporia.knowledge.KnowledgeResult;
import com.aporia.knowledge.WikidataKnowledgeSource;
import com.aporia.knowledge.WikipediaKnowledgeSource;
import com.aporia.knowledge.http.DefaultHttpTransport;
import com.aporia.knowledge.http.HttpTransport;
import com.aporia.ui.panels.ConceptDetailPanel;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class AporiaApp extends Application {

    private VisualGraph visualGraph;
    private Camera camera;
    private GraphRenderer renderer;
    private KnowledgeAggregator aggregator;
    private ConceptDetailPanel detailPanel;
    
    private Graph activeGraph;
    
    // Request counter to handle search race conditions
    private int searchRequestCounter = 0;
    private boolean isWorking = false;
    
    private Stack<String> searchHistory = new Stack<>();
    private Stack<ExpansionSnapshot> expansionHistory = new Stack<>();
    private Set<String> expandedConceptIds = new HashSet<>();
    
    private Button backBtn;
    private TextField searchField;
    private Label statusLabel;
    
    private static class ExpansionSnapshot {
        Graph graph;
        Map<Node, NodeLayout> layoutMap;
        Set<String> expandedConceptIds;
        String selectedNodeId;
        double targetXOffset;
        double targetYOffset;
        double targetZoom;

        public ExpansionSnapshot(Graph graph, Map<Node, NodeLayout> layoutMap, Set<String> expandedConceptIds, String selectedNodeId, double tx, double ty, double tz) {
            this.graph = graph.copy();
            this.layoutMap = layoutMap;
            this.expandedConceptIds = new HashSet<>(expandedConceptIds);
            this.selectedNodeId = selectedNodeId;
            this.targetXOffset = tx;
            this.targetYOffset = ty;
            this.targetZoom = tz;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        boolean reducedMotion = false;

        visualGraph = new VisualGraph();
        camera = new Camera();
        camera.setReducedMotion(reducedMotion);
        
        HttpTransport http = new DefaultHttpTransport();
        aggregator = new KnowledgeAggregator(
            new WikipediaKnowledgeSource(http),
            new WikidataKnowledgeSource(http)
        );

        Canvas canvas = new Canvas(800, 600);
        renderer = new GraphRenderer(canvas, visualGraph, camera);
        renderer.setReducedMotion(reducedMotion);

        new InteractionHandler(canvas, camera, visualGraph, this::handleNodeClick);

        searchField = new TextField();
        searchField.setPromptText("Search knowledge...");
        searchField.setStyle("-fx-background-color: #2A2A28; -fx-text-fill: #F0EAD6; -fx-prompt-text-fill: #A69F91; -fx-border-color: #B59E80; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-padding: 5px 10px;");
        
        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #B59E80; -fx-text-fill: #1A1A18; -fx-background-radius: 3px; -fx-padding: 5px 15px; -fx-font-weight: bold; -fx-cursor: hand;");
        
        backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #B59E80; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5px 10px;");
        backBtn.setDisable(true);
        backBtn.setOnAction(e -> handleBack());
        
        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #A69F91;");
        
        HBox searchBox = new HBox(10, backBtn, searchField, searchBtn, statusLabel);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(20));
        searchBox.setPickOnBounds(false);
        
        Runnable triggerSearch = () -> {
            if (isWorking) return;
            String query = searchField.getText().trim();
            if (query.isEmpty()) return;
            performSearch(query, true);
        };

        searchBtn.setOnAction(e -> triggerSearch.run());
        searchField.setOnAction(e -> triggerSearch.run());
        
        detailPanel = new ConceptDetailPanel(
            conceptId -> performExplore(conceptId),
            node -> {
                visualGraph.selectNode(node);
                handleNodeClick(node);
            }
        );

        StackPane root = new StackPane();
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        
        root.getChildren().addAll(canvas, searchBox, detailPanel);
        StackPane.setAlignment(searchBox, Pos.TOP_LEFT);
        StackPane.setAlignment(detailPanel, Pos.CENTER_RIGHT);
        StackPane.setMargin(detailPanel, new Insets(80, 20, 20, 20));

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
        
        activeGraph = new Graph();
        Node initialNode = new Node("welcome", "Aporia Observatory");
        activeGraph.addNode(initialNode);
        visualGraph.initializeFromDomain(activeGraph, initialNode);
    }
    
    private void handleNodeClick(VisualNode node) {
        if (node == null) {
            detailPanel.update(null, null);
            return;
        }
        detailPanel.update(node, visualGraph);
    }
    
    private void handleBack() {
        if (isWorking) return;
        
        if (!expansionHistory.isEmpty()) {
            ExpansionSnapshot snapshot = expansionHistory.pop();
            activeGraph = snapshot.graph;
            expandedConceptIds = snapshot.expandedConceptIds;
            
            visualGraph.expandFromDomain(activeGraph, snapshot.layoutMap);
            camera.restore(snapshot.targetXOffset, snapshot.targetYOffset, snapshot.targetZoom);
            
            if (snapshot.selectedNodeId != null) {
                VisualNode sel = visualGraph.getNodes().stream()
                    .filter(n -> n.getDomainNode().getId().equals(snapshot.selectedNodeId))
                    .findFirst().orElse(null);
                visualGraph.selectNode(sel);
                detailPanel.update(sel, visualGraph);
            } else {
                visualGraph.selectNode(null);
                detailPanel.update(null, visualGraph);
            }
            
            updateBackButton();
        } else if (!searchHistory.isEmpty()) {
            String prevConcept = searchHistory.pop();
            performSearch(prevConcept, false);
        }
    }
    
    private void updateBackButton() {
        backBtn.setDisable(expansionHistory.isEmpty() && searchHistory.isEmpty());
    }

    private void performSearch(String query, boolean pushHistory) {
        isWorking = true;
        final int currentRequestId = ++searchRequestCounter;
        
        if (pushHistory) {
            visualGraph.getNodes().stream()
                .filter(vn -> vn.getDepth() == 0)
                .findFirst()
                .ifPresent(rootNode -> {
                    if (!rootNode.getDomainNode().getId().equals("welcome")) {
                        searchHistory.push(rootNode.getDomainNode().getLabel());
                    }
                });
        }
        
        Platform.runLater(() -> {
            statusLabel.setText("Searching...");
            statusLabel.setStyle("-fx-text-fill: #A69F91;");
            updateBackButton();
        });
        
        Thread thread = new Thread(() -> {
            try {
                KnowledgeResult result = aggregator.search(query);
                Platform.runLater(() -> {
                    try {
                        if (currentRequestId == searchRequestCounter) {
                            isWorking = false;
                            statusLabel.setText("");
                            searchField.setText(result.primaryConcept().title());
                            
                            expansionHistory.clear();
                            expandedConceptIds.clear();
                            
                            activeGraph = new Graph();
                            KnowledgeMapper.appendToGraph(activeGraph, result);
                            Node rootNode = activeGraph.getNode(result.primaryConcept().id());
                            
                            visualGraph.initializeFromDomain(activeGraph, rootNode);
                            expandedConceptIds.add(rootNode.getId());
                            
                            VisualNode rootVisualNode = visualGraph.getNodes().stream()
                                .filter(vn -> vn.getDomainNode().equals(rootNode))
                                .findFirst().orElse(null);
                            visualGraph.selectNode(rootVisualNode);
                            detailPanel.update(rootVisualNode, visualGraph);
                            
                            camera.reset();
                            updateBackButton();
                        }
                    } catch (Exception e) {
                        if (currentRequestId == searchRequestCounter) {
                            isWorking = false;
                            statusLabel.setText("Mapping failed.");
                            statusLabel.setStyle("-fx-text-fill: #E35353;");
                            System.err.println("Search mapping error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            } catch (KnowledgeException ex) {
                Platform.runLater(() -> {
                    if (currentRequestId == searchRequestCounter) {
                        isWorking = false;
                        statusLabel.setText("Search failed.");
                        statusLabel.setStyle("-fx-text-fill: #E35353;");
                        
                        if (pushHistory && !searchHistory.isEmpty()) {
                            searchHistory.pop();
                        }
                        updateBackButton();
                    }
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    private void performExplore(String conceptId) {
        if (expandedConceptIds.contains(conceptId)) {
            return;
        }
        
        isWorking = true;
        final int currentRequestId = ++searchRequestCounter;
        
        VisualNode visualNode = visualGraph.getNodes().stream()
            .filter(n -> n.getDomainNode().getId().equals(conceptId))
            .findFirst().orElse(null);
            
        if (visualNode == null) {
            isWorking = false;
            return;
        }
        
        Node exploredNode = visualNode.getDomainNode();
        
        // Phase 6.3: Prefer stable Wikidata Q-ID identity to prevent duplication/disconnects
        String query = exploredNode.getId().matches("^Q\\d+$") 
            ? exploredNode.getId() 
            : exploredNode.getLabel(); 
        
        // Snapshot current state
        String selectedId = visualGraph.getSelectedNode() != null ? visualGraph.getSelectedNode().getDomainNode().getId() : null;
        ExpansionSnapshot snapshot = new ExpansionSnapshot(
            activeGraph, 
            visualGraph.getLayoutMap(), 
            expandedConceptIds, 
            selectedId,
            camera.getTargetXOffset(), 
            camera.getTargetYOffset(), 
            camera.getZoom()
        );
        
        Platform.runLater(() -> {
            statusLabel.setText("Exploring...");
            statusLabel.setStyle("-fx-text-fill: #A69F91;");
        });
        
        Thread thread = new Thread(() -> {
            try {
                // Explore by textual query string matching the node's label
                KnowledgeResult result = aggregator.search(query);
                Platform.runLater(() -> {
                    try {
                        if (currentRequestId == searchRequestCounter) {
                            isWorking = false;
                            statusLabel.setText("");
                            expansionHistory.push(snapshot);
                            updateBackButton();
                            
                            List<Node> newNodes = KnowledgeMapper.appendToGraph(activeGraph, result);
                            Map<Node, NodeLayout> layoutMap = visualGraph.getLayoutMap();
                            
                            IncrementalLayout.expand(layoutMap, exploredNode, newNodes, 150.0);
                            
                            visualGraph.expandFromDomain(activeGraph, layoutMap);
                            expandedConceptIds.add(exploredNode.getId());
                            
                            // Keep the explored node selected
                            VisualNode sel = visualGraph.getNodes().stream()
                                .filter(n -> n.getDomainNode().getId().equals(exploredNode.getId()))
                                .findFirst().orElse(null);
                            visualGraph.selectNode(sel);
                            detailPanel.update(sel, visualGraph);
                        }
                    } catch (Exception e) {
                        if (currentRequestId == searchRequestCounter) {
                            isWorking = false;
                            statusLabel.setText("Explore mapping failed.");
                            statusLabel.setStyle("-fx-text-fill: #E35353;");
                            System.err.println("Explore mapping error: " + e.getMessage());
                            e.printStackTrace();
                            if (!expansionHistory.isEmpty()) {
                                expansionHistory.pop();
                                updateBackButton();
                            }
                        }
                    }
                });
            } catch (KnowledgeException ex) {
                Platform.runLater(() -> {
                    if (currentRequestId == searchRequestCounter) {
                        isWorking = false;
                        statusLabel.setText("Explore failed.");
                        statusLabel.setStyle("-fx-text-fill: #E35353;");
                    }
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
