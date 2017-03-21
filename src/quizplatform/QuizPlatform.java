package quizplatform;

/**
 *
 * @author koul1o
 */


import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class QuizPlatform extends Application {

    @Override
    public void start(Stage primaryStage) {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        
        Bridge bridge = new Bridge(engine,primaryStage);

        engine.load(getClass().getResource("html/start.html").toExternalForm());
       
        engine.setJavaScriptEnabled(true);
        
        //StackPane root = new StackPane(webView);
        //root.getChildren().add(webView);
        ProgressBar p2 = new ProgressBar();
        p2.setProgress(0.25F);
        BorderPane root = new BorderPane(webView, p2, null, null, null); 
        root.setAlignment(p2,Pos.CENTER);
        Scene scene = new Scene(root,1000,800);
        scene.getStylesheets().add("caspian.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}