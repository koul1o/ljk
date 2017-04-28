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
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.reactfx.util.FxTimer;

public class QuizPlatform extends Application {

    private static String[] arguments;
    double percent = 0.0;
    private Bridge bridge;
    private float tTime = 60;
    private float fTime = 20;
    private float step = 4;
    private String root = "html/math";

    ProgressBar progressBar = new ProgressBar();

    @Override
    public void start(Stage primaryStage) {
        argsToProperties(arguments);
        /* Create the WebView and WebEngine */
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        setProperties();

        /* Initialize the Bridge */
        bridge = new Bridge(engine, primaryStage, this, tTime, fTime, step, root);

        /* Load the first Url */
        engine.load(getClass().getResource(root + "/documents.html").toExternalForm());

        /* Enable JS in the WebEngine */
        engine.setJavaScriptEnabled(true);

        /* Create a progress bar */
        progressBar.setProgress(percent);

        /* Add progress bar and webView in top and center of a BorderPane */
        BorderPane root = new BorderPane(webView, null, null, progressBar, null);

        /* Align the process bar on the center */
        root.setAlignment(progressBar, Pos.CENTER);

        /* Set the scene containing the BorderPane we created and set the size of it */
        Scene scene = new Scene(root, 1000, 800);

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
        try {
            if (System.getProperty("fullscreen").equals("y")) {
                primaryStage.initStyle(StageStyle.UNDECORATED);
                primaryStage.setFullScreen(true);
                primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            }
        } catch (Exception e) {
            System.out.println("Property Fullscreen missing. To change this parameter set fullscreen=y in run.bat");

        }

        primaryStage.show();

        primaryStage.setOnCloseRequest(exit());
    }

    /* Handles the platform exit. Collects the last trace prior to exit*/
    public EventHandler<WindowEvent> exit() {
        return (WindowEvent event) -> {
            bridge.exit();
        };
    }

    /**
     * Convert command line arguments into system properties
     *
     * @param args
     */
    public static void argsToProperties(String[] args) {
        // Go through all the parameters

        for (int i = 0; i < args.length; i++) {
            String[] nameval = args[i].split("=");

            // Does it have the parameter format : name=value
            if (nameval.length == 2) {
                // Remove the '," and additional spaces if needed
                nameval[1].replace('"', ' ');
                nameval[1].replace('\'', ' ');
                // Set the property
                System.setProperty(nameval[0].trim(), nameval[1].trim());

            }
        }
    }

    public void setProperties() {

        try {
            tTime = Integer.parseInt(System.getProperty("tTime"));
        } catch (NumberFormatException e) {
            System.out.println("Property Training Time missing, default value set: " + tTime + "  To change this parameter set tTime=minutes in run.bat");
        }
        try {
            fTime = Integer.parseInt(System.getProperty("fTime"));
        } catch (NumberFormatException e) {
            System.out.println("Property Final Quiz Time missing, default value set: " + fTime + "  To change this parameter set fTime=minutes in run.bat");
        }
        try {
            step = Integer.parseInt(System.getProperty("step"));
        } catch (NumberFormatException e) {
            System.out.println("Property Step missing, default value set: " + step + "  To change this parameter set step=number of steps in run.bat");
        }
        try {
            this.root = "html/"+(String) System.getProperty("root");
        } catch (NumberFormatException e) {
            System.out.println("Property Root missing, default value set: " + root + "  To change this parameter set root=name (available folders: psych,math) of setup in run.bat");
        }

    }

    public static void main(String[] args) {

        arguments = args;

        launch(args);
    }

}
