package com.aporia.ui.panels;

import com.aporia.model.Node;
import com.aporia.ui.state.VisualEdge;
import com.aporia.ui.state.VisualGraph;
import com.aporia.ui.state.VisualNode;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConceptDetailPanel extends VBox {

    private final Label titleLabel;
    private final Text descriptionText;
    private final VBox relationsBox;
    private final Button exploreBtn;
    
    private final Consumer<String> onExplore;
    private VisualNode currentNode;

    public ConceptDetailPanel(Consumer<String> onExplore) {
        this.onExplore = onExplore;
        
        // Archival Observatory styling
        setStyle("-fx-background-color: rgba(26, 26, 24, 0.90); -fx-border-color: #B59E80; -fx-border-width: 1px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        setPadding(new Insets(20));
        setSpacing(15);
        setPrefWidth(320);
        setMaxWidth(350);
        
        titleLabel = new Label();
        titleLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #F0EAD6;");
        titleLabel.setWrapText(true);
        
        descriptionText = new Text();
        descriptionText.setFont(Font.font("SansSerif", 14));
        descriptionText.setStyle("-fx-fill: #A69F91;");
        descriptionText.setWrappingWidth(280);
        
        Label relationsTitle = new Label("RELATIONSHIPS");
        relationsTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));
        relationsTitle.setStyle("-fx-text-fill: #B59E80;");
        
        relationsBox = new VBox(8); // spacing
        
        Label sourcesTitle = new Label("SOURCES");
        sourcesTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));
        sourcesTitle.setStyle("-fx-text-fill: #B59E80;");
        
        Label sourcesText = new Label("Aggregated Knowledge\n(Provenance not tracked per-node)");
        sourcesText.setFont(Font.font("SansSerif", 13));
        sourcesText.setStyle("-fx-text-fill: #A69F91;");
        
        exploreBtn = new Button("Explore Concept");
        exploreBtn.setStyle("-fx-background-color: #B59E80; -fx-text-fill: #1A1A18; -fx-background-radius: 3px; -fx-padding: 8px 15px; -fx-font-weight: bold; -fx-cursor: hand;");
        exploreBtn.setMaxWidth(Double.MAX_VALUE);
        exploreBtn.setOnAction(e -> {
            if (currentNode != null && onExplore != null) {
                onExplore.accept(currentNode.getDomainNode().getId());
            }
        });
        
        VBox scrollContent = new VBox(20, descriptionText, relationsTitle, relationsBox, sourcesTitle, sourcesText);
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
        
        relationsBox.getChildren().clear();
        
        // Find both outgoing and incoming edges connected to this node
        var connectedEdges = graph.getEdges().stream()
                .filter(e -> e.getSource().equals(node) || e.getTarget().equals(node))
                .collect(Collectors.toList());
                
        if (connectedEdges.isEmpty()) {
            Label noRels = new Label("No relationships available.");
            noRels.setStyle("-fx-text-fill: #A69F91; -fx-font-size: 13px;");
            relationsBox.getChildren().add(noRels);
        } else {
            for (VisualEdge edge : connectedEdges) {
                boolean isOutgoing = edge.getSource().equals(node);
                String relatedLabel = isOutgoing 
                    ? edge.getTarget().getDomainNode().getLabel()
                    : edge.getSource().getDomainNode().getLabel();
                    
                String relType = edge.getDomainEdge().relationship();
                relType = relType.replace("_", " ").toLowerCase();
                
                // Add directional context for clarity on incoming edges
                if (!isOutgoing) {
                    relType = "incoming: " + relType;
                }
                
                Label targetLbl = new Label(relatedLabel);
                targetLbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
                targetLbl.setStyle("-fx-text-fill: #F0EAD6;");
                
                Label relLbl = new Label(relType);
                relLbl.setFont(Font.font("SansSerif", 12));
                relLbl.setStyle("-fx-text-fill: #A69F91;");
                
                VBox relPair = new VBox(2, targetLbl, relLbl);
                relationsBox.getChildren().add(relPair);
            }
        }
        
        exploreBtn.setDisable(node.getDepth() == 0); // Root is already explored
        setVisible(true);
    }
}
