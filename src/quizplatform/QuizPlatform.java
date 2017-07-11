package quizplatform;

import java.net.URL;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * <h1>QuizPlatform</h1>
 * The QuizPlatform class contains the main class of the program. It is
 * responsible for: <ol>
 * <li> Creating the JavaFX application</li>
 * <li> Creating and positioning visual elements of the platform</li>
 * <li> Extracting system properties from the execution arguments</li>
 * <li> Setting up the window and on-window behaviors</li>
 * </ol>
 * <p>
 * <b>1.</b> To create the JavaFX application and display the content we need to
 * create a <b>WebEngine</b> that will load Web pages, create their document models,
 * apply styles as necessary, run JavaScript on pages, and a <b>WebView</b> that will
 * manage the WebEngine and displays its content. To display the content we are
 * creating a <b>scene</b> with given initial dimensions and we set it as the <b>primary
 * stage</b>. An event handler is responsible for exiting the platform.
 * <p>
 * <b>2.</b> To keep track of the time we are providing the user with a
 * <b>progress bar</b> {@code ProgressBar {@code progressBar = new ProgressBar()}} that
 * is being augmented by the percent variable. The progress bar progression is
 * managed by the
 * {@link quizplatform.Bridge#setTimersAndProgressBar(javafx.scene.control.ProgressBar, float, float, float)} functioon.
 * The <i>scene</i> is brought together by a <b>BorderPane</b> where at the
 * center we have the WebView and at the bottom the ProgressBar. The
 * <i>caspian.css</i> contains style options for the progress bar.
 * <p>
 * <b>3.</b> When executing the platform, command line <b>arguments</b> can be
 * passed to alter the experimental setting such as training time (tTime), final
 * quiz time (fTime), progression of the progress bar (step), highlighting
 * option, full screen options, the experimentId and the setup/folder of content
 * (e.g. psych, math, stat). These options are converted into <b>system
 * properties</b> using the argsToProperties(String[] args) function. These
 * system properties are later converted into variables by the setProperties()
 * function. The <b>full screen</b> is set at the start function.
 * <p>
 * <b>4.</b> Other window settings while on full screen include disabling the
 * <b>window title bar buttons</b>:
 * {@code primaryStage.initStyle(StageStyle.UNDECORATED);} and disabling
 * <b>window close key combinations</b> (alt+f4):
 * {@code primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);}.
 * Furthermore, when the highlighting option is selected the
 * createContextMenu(WebView webView, Bridge bridge) function creates a new
 * Context Menu that only has the Highlight option (when right click). Then the
 * createBaseContextMenu(WebView webView, Bridge bridge) function disables the
 * context menu on images to avoid problems.
 *
 * @author koul1o
 */
public class QuizPlatform extends Application {

    private static String[] arguments;
    private Bridge bridge;
    private float tTime = 60;
    private float fTime = 20;
    private float step = 4;
    private String root = "html/stat_design_1";
    private double percent = 0;
    private ProgressBar progressBar = new ProgressBar();
    private String experimentId = "00000";
    private Boolean highlightEnabled = true;

    @Override
    public void start(Stage primaryStage) {
        argsToProperties(arguments);
        /* Create the WebView and WebEngine */
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        setProperties();
        /* Initialize the Bridge */
        bridge = new Bridge(engine, primaryStage, this, tTime, fTime, step, root, experimentId, progressBar);
        if (this.highlightEnabled) {
            webView.setContextMenuEnabled(false);
            createContextMenu(webView, bridge);
        } else {
            createBaseContextMenu(webView, bridge);
        }

        /* Enable JS in the WebEngine */
        engine.setJavaScriptEnabled(true);

        engine.load(getClass().getResource("/bin/quizplatform/" + root + "/Instructions.html").toExternalForm());

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

    /**
     * Handles the platform exit.
     *
     * @return exit the bridge
     */
    public EventHandler<WindowEvent> exit() {
        return (WindowEvent event) -> {
            bridge.exit();
        };
    }

    /**
     * Convert command line arguments into system properties. <br>
     * The command line arguments can be set through the "run" files.
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

    /**
     * This function converts the system properties to variables.<br>
     * If a property is not filled as message is being prompted.
     */
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
            if (!System.getProperty("root").isEmpty()) {
                this.root = "html/" + (String) System.getProperty("root");
            }
        } catch (NullPointerException e) {
            System.out.println("Property Root missing, default value set: " + root + "  To change this parameter set root=name (available folders: psych,math) of setup in run.bat");
        }
        try {
            if (!System.getProperty("experimentId").isEmpty()) {
                this.experimentId = (String) System.getProperty("experimentId");
            }
        } catch (NullPointerException e) {
            System.out.println("Property Experiment ID missing, default value set: " + this.experimentId + "  To change this parameter set experimentId=id of setup in run.bat");
        }
        try {
            if (!System.getProperty("highlightEnabled").isEmpty()) {
                this.highlightEnabled = Boolean.valueOf(System.getProperty("highlightEnabled"));
            }
        } catch (NullPointerException e) {
            System.out.println("Property highlight enabled missing, default value set: " + this.highlightEnabled + "  To change this parameter set highlightEnabled=boolean (true or false) of setup in run.bat");
        }

    }

    /**
     * Creates a new Context Menu that only has the Highlight option.
     *
     * @param webView the WebView object in which to create the context menu
     * @param bridge the Bridge object that will send the actions to the
     * javascript
     */
    private void createContextMenu(WebView webView, Bridge bridge) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem highlight = new MenuItem("Highlight");
        highlight.setOnAction(e -> bridge.checkHighlight());
        contextMenu.getItems().addAll(highlight);

        webView.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(webView, e.getScreenX(), e.getScreenY());
            } else {
                contextMenu.hide();
            }
        });
    }

    /**
     * Disables the context menu on images to avoid problems
     *
     * @param webView the WebView object in which to create the context menu
     * @param bridge the Bridge object that will send the actions to the
     * javascript.
     */
    private void createBaseContextMenu(WebView webView, Bridge bridge) {
        webView.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                String s = webView.getEngine().executeScript("document.elementFromPoint(" + e.getX() + "," + e.getY() + ").tagName;").toString();
                webView.setContextMenuEnabled(true);
                if (s.equals("IMG")) {
                    webView.setContextMenuEnabled(false);
                }
            }
        });
    }

    public static void main(String[] args) {

        arguments = args;

        launch(args);
    }

}
