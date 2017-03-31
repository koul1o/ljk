package quizplatform;

/**
 *
 * @author koul1o
 */


import java.net.URL;
import java.time.Duration;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

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
        BorderPane root = new BorderPane(webView, null, null, p2, null); 
        
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
        
        /* Using org.reactfx.util.FxTimer augment the progress bar periodicaly every 15min by 25% */
        FxTimer.runPeriodically(
        Duration.ofMillis(900000),
        () -> {
            this.p=this.p+0.25;
            p2.setProgress(this.p);
        });
        /* Exit the app after 1h
        FxTimer.runLater(
        Duration.ofMillis(3600000),
        () -> bridge.exit());
        */
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    
    
}