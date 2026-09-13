package aporia;

import com.aporia.graph.Graph;
import com.aporia.graph.layout.Point;
import com.aporia.model.Edge;
import com.aporia.model.Node;
import com.aporia.ui.camera.Camera;
import com.aporia.ui.graph.GraphRenderer;
import com.aporia.ui.state.VisualGraph;
import com.aporia.ui.state.VisualNode;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AporiaApp extends Application {

    private double lastMouseX;
    private double lastMouseY;

    @Override
    public void start(Stage primaryStage) {
        // 1. Create Sample Graph
        Graph graph = new Graph();
        Node astronomy = new Node("astronomy", "Astronomy");
        Node physics = new Node("physics", "Physics");
        Node stars = new Node("stars", "Stars");
        Node gravity = new Node("gravity", "Gravity");
        Node relativity = new Node("relativity", "Relativity");

        graph.addNode(astronomy);
        graph.addNode(physics);
        graph.addNode(stars);
        graph.addNode(gravity);
        graph.addNode(relativity);

        graph.addEdge(new Edge(astronomy, physics, "RELATED"));
        graph.addEdge(new Edge(astronomy, stars, "STUDIES"));
        graph.addEdge(new Edge(physics, gravity, "EXPLAINS"));
        graph.addEdge(new Edge(gravity, relativity, "RELATED"));

        // 2. Initialize Visual State
        VisualGraph visualGraph = new VisualGraph();
        visualGraph.initializeFromDomain(graph, astronomy);

        // 3. Setup Camera and Renderer
        Camera camera = new Camera();
        Canvas canvas = new Canvas(800, 600);
        GraphRenderer renderer = new GraphRenderer(canvas, visualGraph, camera);

        // 4. Setup Resizable Pane
        Pane root = new Pane(canvas);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        // 5. Input Handling
        canvas.setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();

            // Check for node selection
            Point worldClick = camera.screenToWorld(event.getX(), event.getY());
            VisualNode clickedNode = null;
            
            // 20.0 is NODE_RADIUS
            for (VisualNode vNode : visualGraph.getNodes()) {
                double dx = vNode.getX() - worldClick.x();
                double dy = vNode.getY() - worldClick.y();
                if (Math.hypot(dx, dy) <= 20.0) {
                    clickedNode = vNode;
                    break;
                }
            }
            
            visualGraph.selectNode(clickedNode);
        });

        canvas.setOnMouseDragged(event -> {
            double dx = event.getX() - lastMouseX;
            double dy = event.getY() - lastMouseY;
            camera.pan(dx, dy);
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        canvas.setOnScroll(event -> {
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            camera.zoom(zoomFactor, event.getX(), event.getY());
        });

        // 6. Animation Loop
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderer.draw();
            }
        };
        timer.start();

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Aporia");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
