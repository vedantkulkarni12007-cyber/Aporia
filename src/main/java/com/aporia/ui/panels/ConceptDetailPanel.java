package com.aporia.ui.panels;

import com.aporia.model.Node;
import com.aporia.ui.state.VisualEdge;
import com.aporia.ui.state.VisualGraph;
import com.aporia.ui.state.VisualNode;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConceptDetailPanel extends VBox {

    private final Label titleLabel;
    private final Text descriptionText;
    private final VBox aboutBox;
    private final VBox relationsBox;
    private final VBox contextBox;
    private final VBox sourcesBox;
    
    private final Button exploreBtn;
    
    private final Consumer<String> onExplore;
    private final Consumer<VisualNode> onSelect;
    private VisualNode currentNode;

    public ConceptDetailPanel(Consumer<String> onExplore, Consumer<VisualNode> onSelect) {
        this.onExplore = onExplore;
        this.onSelect = onSelect;
        
        // Archival Observatory styling
        setStyle("-fx-background-color: rgba(26, 26, 24, 0.90); -fx-border-color: #B59E80; -fx-border-width: 1px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        setPadding(new Insets(20));
        setSpacing(20);
        setPrefWidth(340);
        setMaxWidth(380);
        
        titleLabel = new Label();
        titleLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #F0EAD6;");
        titleLabel.setWrapText(true);
        
        descriptionText = new Text();
        descriptionText.setFont(Font.font("SansSerif", 14));
        descriptionText.setStyle("-fx-fill: #A69F91;");
        descriptionText.setWrappingWidth(290);
        
        aboutBox = new VBox(8, createSectionTitle("ABOUT"), descriptionText);
        
        relationsBox = new VBox(10);
        contextBox = new VBox(10);
        
        Label sourcesText = new Label("Aggregated Knowledge\nProvenance not tracked per-node");
        sourcesText.setFont(Font.font("SansSerif", 13));
        sourcesText.setStyle("-fx-text-fill: #A69F91;");
        sourcesBox = new VBox(8, createSectionTitle("SOURCES"), sourcesText);
        
        exploreBtn = new Button("Explore Concept");
        exploreBtn.setStyle("-fx-background-color: #B59E80; -fx-text-fill: #1A1A18; -fx-background-radius: 3px; -fx-padding: 8px 15px; -fx-font-weight: bold; -fx-cursor: hand;");
        exploreBtn.setMaxWidth(Double.MAX_VALUE);
        exploreBtn.setOnAction(e -> {
            if (currentNode != null && this.onExplore != null) {
                this.onExplore.accept(currentNode.getDomainNode().getId());
            }
        });
        
        VBox scrollContent = new VBox(25, aboutBox, relationsBox, contextBox, sourcesBox);
        scrollContent.setPadding(new Insets(0, 10, 0, 0));
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.getStylesheets().add(createScrollbarStyle());
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        getChildren().addAll(titleLabel, scrollPane, exploreBtn);
        
        setVisible(false);
    }
    
    private Label createSectionTitle(String title) {
        Label lbl = new Label(title);
        lbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));
        lbl.setStyle("-fx-text-fill: #B59E80;");
        return lbl;
    }
    
    private String createScrollbarStyle() {
        return "data:text/css," + 
               ".scroll-pane { -fx-background-color: transparent; } " +
               ".scroll-pane > .viewport { -fx-background-color: transparent; } " +
               ".scroll-bar:vertical { -fx-background-color: transparent; -fx-pref-width: 8px; } " +
               ".scroll-bar:vertical .thumb { -fx-background-color: #A69F91; -fx-background-radius: 4px; }";
    }
    
    public void update(VisualNode node, VisualGraph graph) {
        if (node == null) {
            setVisible(false);
            this.currentNode = null;
            return;
        }
        
        this.currentNode = node;
        Node domainNode = node.getDomainNode();
        
        titleLabel.setText(domainNode.getLabel().toUpperCase());
        
        if (domainNode.getDescription() != null && !domainNode.getDescription().isBlank()) {
            descriptionText.setText(domainNode.getDescription());
        } else {
            descriptionText.setText("No description available.");
        }
        
        // Populate Connected Ideas
        relationsBox.getChildren().clear();
        relationsBox.getChildren().add(createSectionTitle("CONNECTED IDEAS"));
        
        var connectedEdges = graph.getEdges().stream()
                .filter(e -> e.getSource().equals(node) || e.getTarget().equals(node))
                .collect(Collectors.toList());
                
        if (connectedEdges.isEmpty()) {
            Label noRels = new Label("No connected ideas available.");
            noRels.setStyle("-fx-text-fill: #A69F91; -fx-font-size: 13px;");
            relationsBox.getChildren().add(noRels);
        } else {
            for (VisualEdge edge : connectedEdges) {
                boolean isOutgoing = edge.getSource().equals(node);
                VisualNode targetVisual = isOutgoing ? edge.getTarget() : edge.getSource();
                
                String relatedLabel = targetVisual.getDomainNode().getLabel();
                String relType = edge.getDomainEdge().relationship().replace("_", " ").toLowerCase();
                if (!isOutgoing) {
                    relType = "incoming: " + relType;
                }
                
                Label targetLbl = new Label(relatedLabel);
                targetLbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
                targetLbl.setStyle("-fx-text-fill: #F0EAD6;");
                
                Label relLbl = new Label(relType);
                relLbl.setFont(Font.font("SansSerif", 12));
                relLbl.setStyle("-fx-text-fill: #A69F91;");
                
                VBox relPair = new VBox(2, targetLbl, relLbl);
                relPair.setPadding(new Insets(5));
                relPair.setStyle("-fx-background-color: transparent; -fx-background-radius: 3px;");
                
                // Make clickable
                relPair.setCursor(Cursor.HAND);
                relPair.setOnMouseEntered(e -> relPair.setStyle("-fx-background-color: #2A2824; -fx-background-radius: 3px;"));
                relPair.setOnMouseExited(e -> relPair.setStyle("-fx-background-color: transparent; -fx-background-radius: 3px;"));
                relPair.setOnMouseClicked(e -> {
                    if (onSelect != null) {
                        onSelect.accept(targetVisual);
                    }
                });
                
                relationsBox.getChildren().add(relPair);
            }
        }
        
        // Populate Context
        contextBox.getChildren().clear();
        Map<String, List<String>> context = domainNode.getContext();
        if (!context.isEmpty()) {
            contextBox.getChildren().add(createSectionTitle("CONTEXT"));
            for (Map.Entry<String, List<String>> entry : context.entrySet()) {
                String key = entry.getKey().replace("_", " ");
                // Title case key
                key = key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase();
                
                Label keyLbl = new Label(key);
                keyLbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));
                keyLbl.setStyle("-fx-text-fill: #A69F91;");
                
                String valuesStr = String.join(", ", entry.getValue());
                Text valTxt = new Text(valuesStr);
                valTxt.setFont(Font.font("SansSerif", 13));
                valTxt.setStyle("-fx-fill: #F0EAD6;");
                valTxt.setWrappingWidth(280);
                
                VBox ctxPair = new VBox(2, keyLbl, valTxt);
                contextBox.getChildren().add(ctxPair);
            }
            contextBox.setVisible(true);
            contextBox.setManaged(true);
        } else {
            contextBox.setVisible(false);
            contextBox.setManaged(false);
        }
        
        exploreBtn.setDisable(node.getDepth() == 0); // Root is already explored
        setVisible(true);
    }
}
