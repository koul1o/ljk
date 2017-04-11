package quizplatform;

/**
 *
 * @author koul1o
 */


import java.net.URL;
import java.time.Duration;
import javafx.application.Application;
import javafx.event.EventHandler;
import static javafx.application.Application.launch;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.reactfx.util.FxTimer;

public class QuizPlatform extends Application {

    private double percent=0.0;
    public Bridge bridge;
    
    @Override
    public void start(Stage primaryStage) {
    
        /* Create the WebView and WebEngine */
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        
        /* Initialize the Bridge */
        bridge = new Bridge(engine,primaryStage);
        
        /* Load the first Url */
        engine.load(getClass().getResource("html/documents.html").toExternalForm());
       
        /* Enable JS in the WebEngine */
        engine.setJavaScriptEnabled(true);
        
        /* Create a progress bar */
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(percent);
        
        /* Add progress bar and webView in top and center of a BorderPane */
        BorderPane root = new BorderPane(webView, null, null, progressBar, null); 
        
        /* Align the process bar on the center */
        root.setAlignment(progressBar,Pos.CENTER);
        
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
        progressBar.prefWidthProperty().bind(root.widthProperty().subtract(20)); 
        
        /* Set the scene  */
        primaryStage.setScene(scene);
        primaryStage.show();
        
        /* Using org.reactfx.util.FxTimer augment the progress bar periodicaly every 15min by 25% */
        FxTimer.runPeriodically(
        Duration.ofMillis(900000),
        () -> {
            percent+=0.25;
            progressBar.setProgress(percent);
        });
        
        primaryStage.setOnCloseRequest(exit());
    }
    
    
    
    /* Handles the platform exit. Collects the last trace prior to exit*/
    public EventHandler<WindowEvent> exit(){
    	return (WindowEvent event) -> {
            bridge.exit();
            };
    }

    public static void main(String[] args) {
        launch(args);
    }
        
}
