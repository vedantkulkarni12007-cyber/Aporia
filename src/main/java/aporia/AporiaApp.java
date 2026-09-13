package aporia;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class AporiaApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        
        // A minimal placeholder background as requested (charcoal)
        Rectangle background = new Rectangle(800, 600, Color.web("#222222"));
        root.getChildren().add(background);
        
        Scene scene = new Scene(root, 800, 600);
        
        primaryStage.setTitle("Aporia");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
