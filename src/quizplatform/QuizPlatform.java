package quizplatform;

/**
 *
 * @author koul1o
 */


import java.net.URL;
import java.time.Duration;
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
import org.reactfx.EventStreams;

import org.reactfx.util.FxTimer;

public class QuizPlatform extends Application {

    private double p=0.0;

    @Override
    public void start(Stage primaryStage) {
        
        
        
        /* Create the WebView and WebEngine */
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        
        /* Initialize the Bridge */
        Bridge bridge = new Bridge(engine,primaryStage);
        
        /* Load the first Url */
        engine.load(getClass().getResource("html/documents.html").toExternalForm());
       
        /* Enable JS in the WebEngine */
        engine.setJavaScriptEnabled(true);
        
        /* Create a progress bar */
        ProgressBar p2 = new ProgressBar();
        p2.setProgress(p);
        
        /* Add progress bar and webView in top and center of a BorderPane */
        BorderPane root = new BorderPane(webView, p2, null, null, null); 
        
        /* Align the process bar on the center */
        root.setAlignment(p2,Pos.CENTER);
        
        /* Set the scene containing the BorderPane we created and set the size of it */
        Scene scene = new Scene(root,1000,800);
       
        /* Add custom css for the progress bar */
        URL url = this.getClass().getResource("caspian.css");
        if (url == null) {
            System.out.println("Resource not found. Aborting.");
            System.exit(-1);
        }
        String css = url.toExternalForm(); 
        scene.getStylesheets().add(css);
        p2.prefWidthProperty().bind(root.widthProperty().subtract(20)); 
        
        /* Set the scene  */
        primaryStage.setScene(scene);
        primaryStage.show();
        
       
        FxTimer.runPeriodically(
        Duration.ofMillis(250),
        () -> {
            this.p=this.p+0.1;
            p2.setProgress(this.p);
        });
        
        FxTimer.runLater(
        Duration.ofMillis(2500),
        () -> bridge.exit());
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    
    
}