package quizplatform;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.control.ProgressBar;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

/**
 * <h1>Bridge</h1>
 * The Bridge class manages:
 * <ol>
 * <li> The connection with JavaScript (html/name_of_root/js/quizplatform.js or
 * quizplatform.iframe.js)</li>
 * <li> The functionality of the platform and the content of the HTML pages
 * through the constructor</li>
 * <li> The time & the progression of the progressBar of
 * {@link quizplatform.Quizplatform}</li>
 * <li> The collection and storing of traces</li>
 * </ol>
 * <p>
 * <b>1.</b> By registering Bridge to the window, we obtain two way
 * communication between Java and JavaScript. First we need to register a member
 * to the window: {@code FFwindow.setMember("member_name", this);}. We can
 * <b>call JavaScript functions</b> by calling executeScript in engine:
 * {@code engine.executeScript("setDocuments();");}. To perform <b>upcalls</b>
 * from JavaScript to Java we are using the name of the registered member to the
 * window object {@code member_name.getUrl(url);}. The execute() function
 * manages the upcalls from JavaScript.  </p>
 * <p>
 * <b>2.</b> Bridge contains various functions managing the functionality of the
 * platform. Once the Bridge constructor is initialized in
 * {@link quizplatform.Quizplatform}, it takes the parameters such as: tTime,
 * fTime, step, root etc. that will be used here and resets the previous files
 * (to avoid viewing already highlighted files). Using a <b>state Listener</b>
 * {@code engine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> obs, State oldState, State newState)}
 * we observe state changes (e.g. when a new page is being loaded). At <b>fist
 * page load</b> (instructions page) {@code cnt<1} we are calling
 * {@link quizplatform.Bridge#setTimersAndProgressBar(javafx.scene.control.ProgressBar, float, float, float)}
 * to set the timers and the progressBar. In <b>any other page load</b>
 * {@code cnt=<1} we are managing the various aspects of the platform according
 * to a given state and collecting traces when a new state (page load) occurs.
 * When we are inside a document page we perform a <b>hotfix</b>
 * by adding "Panel 1" to the traces since it was not possible to collect this
 * information otherwise and update information about the current document that
 * will be used by JavaScript. Then, when the "documents" page is accessed
 * (documents.html) we are <b>setting all the available documents </b>. If we
 * are not in one of these pages we are just collecting the traces of the
 * visited page.
 * </p>
 * <p>
 * <b>3.</b> As mentioned above, at first page load we are calling the
 * {@link quizplatform.Bridge#setTimersAndProgressBar(javafx.scene.control.ProgressBar, float, float, float)}
 * function to set the timers for the automatic redirection to the pages of
 * final quiz(final_quiz.html) and demographic (info.html) and augmenting or
 * reseting the progressBar. Here, when finishing the training period we are
 * redirecting to the final quiz and starting a new timer. Similarly when the
 * final quiz time is over we redirect to the demographic. In the scenario when
 * a user submits the final quiz prior to the end of the designated time we are
 * using the {@link quizplatform.Bridge#submitFinalQuiz()} function to reset the
 * timer and progress bar.
 * </p>
 * <p>
 * <b>4.</b> Saving the traces is done through
 * {@link quizplatform.Bridge#getTrace(java.lang.String)} function that outputs
 * the trace in the console and calls
 * {@link quizplatform.Bridge#saveData(java.lang.String)} and
 * {@link quizplatform.Bridge#saveData(java.lang.String, java.lang.String)}
 * functions that save the trace to a .csv file.
 * </p>
 *
 * @author koul1o
 */
public class Bridge {

    private static final String QUESTION_NAME = "question";
    private static final String[] FORBIDDEN_WORDS = {QUESTION_NAME, "info", "final_quiz", "manual", "documents"};
    private int time = 0;
    private JSObject window;
    private String title;
    private WebEngine engine;
    private String docUrl = null;
    private final LongProperty startTime = new SimpleLongProperty();
    private final LongProperty endTime = new SimpleLongProperty();
    private final LongProperty elapsedTime = new SimpleLongProperty();
    private int cnt = 0;
    private String traceT = "";
    private boolean firstStat = true;
    private String experimentId = "";
    private String fullFilepath = "";
    private HashMap<String, String> quizLinks;
    private static String[][] files;
    private static String[][] allFiles;
    private static final float MILIS = 60000;
    private float augmentBar;
    private Timer timer2;
    private String setup;
    private float tTime, fTime;
    private double percent;
    private String previousUrl = "";
    private String changedHtml = "";
    private String section = "";
    private String srcPath = "";
    private String binPath = "";
    private Timer demogTimer;
    private Timer t;

    public Bridge(WebEngine engine, Stage stage, QuizPlatform quizPlatform, float tTime, float fTime, float step, String root, String experimentId, ProgressBar progressBar) {
        String DOCUMENT_PATH = "./src" + File.separator + "quizplatform" + File.separator + root;

        this.setup = root;
        this.setup = this.setup.replace("html/", "");
        this.srcPath = DOCUMENT_PATH;
        this.binPath = this.srcPath.replace("src", "bin");
        this.quizLinks = new HashMap<String, String>();
        try {
            findFiles(new File(DOCUMENT_PATH));

            this.resetFiles();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Observe state changes. (page loads)
        engine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> obs, State oldState, State newState) -> {

            if (newState == State.SUCCEEDED) {

                this.engine = engine;
                window = (JSObject) engine.executeScript("window");
                /* Register our java app to the window so that we can make upcalls to the app using java.functionName(); */
                window.setMember("java", this);
                title = engine.getTitle();
                stage.setTitle(engine.getTitle());

                /**
                 * FIRST (start/instructions) PAGE LOAD - cnt <1, to set the
                 * timers and what to run later.
                 */
                if (cnt < 1) {
                    //saveData("Time_Location_Area_Value");
                    startTime.set(System.nanoTime());
                    getTime();
                    //Hotfix for default Panel trace. If load a document page, add "_Panel 1" and collect trace. 
                    traceT = time + "_" + title + "_Panel 1";
                    getTrace(traceT);
                    docUrl = engine.getLocation();
                    docUrl = docUrl.replace("file://", "");
                    manageQuiz();
                    setTimersAndProgressBar(progressBar, step, tTime, fTime);
                    cnt++;

                } /**
                 * NOT FIRST PAGE LOAD - cnt >1.
                 */
                else {
                    //Hotfix for default Panel trace. If load a document page, add "_Panel 1" and collect trace. In any other case save the trace as is. 
                    if (!(title.toLowerCase().contains("final") || title.toLowerCase().contains("documents") || title.toLowerCase().contains("demographic"))) {
                        getTime();
                        docUrl = engine.getLocation();
                        docUrl = docUrl.replace("file://", "");
                        this.section = "_Panel 1";
                        traceT = time + "_" + title + this.section;
                        getTrace(traceT);
                    } //When in "documents" page get all available docs with getDocuments and call setDocuments in JS to add the doc urls and titles in the html. 
                    else if (title.toLowerCase().contains("documents")) {
                        getDocuments();
                        engine.executeScript("setDocuments();");
                        getTime();
                        traceT = time + "_" + title;
                        getTrace(traceT);
                    } else {
                        getTime();
                        traceT = time + "_" + title;
                        getTrace(traceT);
                    }
                }
                manageQuiz();
            }
        });
    }

    /**
     * This function manages the progression of the questions by updating the
     * qUrl. If qUlr=='#' there is no more questions left.
     */
    public void manageQuiz() {
        if (docUrl != null) {
            // add the doc into the hashmap if it doesn't exist yet then update the quiz URL
            if (!this.quizLinks.containsKey(docUrl) && !title.toLowerCase().contains(QUESTION_NAME)) {
                this.quizLinks.put(docUrl, docUrl.replace(".html", "_" + QUESTION_NAME + "1.html"));
            }
            if (this.quizLinks.get(docUrl) != null && this.quizLinks.get(docUrl).contains("_" + QUESTION_NAME)) {
                engine.executeScript("var qUrl=\'" + this.quizLinks.get(docUrl) + "\'");
            } else {
                engine.executeScript("var qUrl='#'");
            }
        }
    }

    /**
     * Function, to exit the platform
     */
    public void exit() {

        getLastTrace();
        Platform.exit();

    }

    /**
     * This function collects the traces.
     */
    public void getTrace(String trace) {
        System.out.println("Trace: " + trace);
        saveData(trace);
        if (!this.previousUrl.equals(this.engine.getLocation()) && !this.changedHtml.equals("")) {
            this.savePage();
        }

    }

    /**
     * This function is called upon sudden exit of the platform to save the last
     * trace.
     *
     */
    public void getLastTrace() {
        getTime();
        traceT = time + "_" + title + "_Exit";
        getTrace(traceT);
    }

    /**
     * This function collects traces that contain elements such as Panel numbers
     * and question numbers.
     *
     * @param element a trace string that also contains the name of the element
     */
    public void elementTrace(String element) {
        getTime();
        this.section = element;
        traceT = time + "_" + title + "_" + element;
        getTrace(traceT);
    }

    /**
     * This function updates the time in ms.
     */
    public void getTime() {
        endTime.set(System.nanoTime());
        elapsedTime.bind(Bindings.subtract(endTime, startTime));
        time = (int) (0 + elapsedTime.divide(1_000_000).getValue());
    }

    /**
     * Up-call to this function from the page, to update the next question URL
     * for a document quiz.
     *
     * @param url - The current quiz URL
     */
    public void getUrl(String url) {
        URLToNextQuestion(url);
        engine.executeScript("var qUrl=\'" + this.quizLinks.get(docUrl) + "\'");
        if (!this.quizLinks.get(docUrl).contains("finished")) {
            engine.executeScript("redirect();");
        } else {
            engine.executeScript("afterSubmit();");
        }
    }

    /**
     * This function once called from the JavaScript, it restarts the quiz.
     */
    public void restartQuiz() {
        String docUrltmp = docUrl.replace(".html", "_question1.html");
        this.quizLinks.replace(docUrl, docUrltmp);
        engine.executeScript("var qUrl=\'" + this.quizLinks.get(docUrl) + "\'");
    }

    /**
     * This function sends an double dimension array to the JavaScript
     * containing the name of the documents and their URLs.<br>
     * The URLs are in the first column, the names are in the second. <br>
     * The array is stored in the <b>docs</b> variable in the JavaScript.
     */
    public void getDocuments() {
        String s = "var docs = [";
        for (int i = 0; i < Bridge.files[0].length; i++) {
            s = s + "[\'" + Bridge.files[0][i] + "\',\'" + Bridge.files[1][i] + "\']";
            if (i != Bridge.files[0].length - 1) {
                s = s + ",";
            }
        }
        s = s + "];";
        engine.executeScript(s);
    }

    /**
     * This function changes the String <b>quizUrl</b> by adding 1 to the number
     * of the quiz. So C://example/document1_quiz1.html would become
     * C://example/document1_quiz2.html.<br>
     * It takes the path to the HTML file then parses it and changes only the
     * last number. <br>
     * It then changes the entry of the document in the <b>quizLinks</b> hashmap
     * so that the value matches the path of the next question. <br>
     * <br>
     * If the resulting file does not exist, the URL is set to "finished". <br>
     *
     * @param quizUrl the URL to save
     */
    public void URLToNextQuestion(String quizUrl) {

        String r = this.incrementString(quizUrl);
        try {
            r = URLDecoder.decode(r, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        File f = new File(r);
        if (!f.exists()) {
            String s[] = r.split("/");
            r = "";

            s[s.length - 1] = "finished";
            int i = 0;
            for (i = 0; i < s.length; i++) {
                if (i != 0) {
                    r = r + "/" + s[i];
                } else {
                    r = r + s[i];
                }
            }

        }
        this.quizLinks.replace(docUrl, r);
    }

    /**
     * Adds one to the last digit of the argument
     *
     * @param sti the String to increment
     * @return the modified String
     */
    public String incrementString(String sti) {
        Pattern digitPattern = Pattern.compile("(\\d+)");

        Matcher matcher = digitPattern.matcher(sti);
        StringBuffer result = new StringBuffer();
        int index = 0;
        while (matcher.find()) {
            index = matcher.start();
        }
        matcher.find(index);
        matcher.appendReplacement(result, String.valueOf(Integer.parseInt(matcher.group(1)) + 1));
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * This function redirects us to the next question while in the quiz.
     *
     * @param url the current quiz url.
     */
    public void redirect(String url) {
        engine.executeScript("window.location.replace(\'" + url + "\');");
    }

    /**
     * This function goes through all files contained in the <b>directory</b>
     * path. If the file is a document, then it adds it to the returning
     * array.<br/>
     * The entries of the array have two values, the first one is the canonical
     * path to the file and the second is the name of the file without its
     * extension.<br/>
     *
     * @param directory The path to the directory to explore
     * @return An array of two dimensions. The first dimension contains the
     * canonical path (0) and the filename (1), the second dimension is the
     * entries
     * @throws IOException If an I/O error occurs, which is possible because the
     * construction of the canonical pathname may require filesystem queries.
     */
    public static void findFiles(File directory) throws IOException {
        File[] file;
        HashMap<String, String> al = new HashMap<String, String>();
        HashMap<String, String> allFileList = new HashMap<String, String>();
        if (directory.isDirectory()) {
            file = directory.listFiles(); // Calls same method again.
            for (File f : file) {
                if (f.isDirectory()) {
                    // findFiles(f);
                } else {

                    System.out.println(f.getAbsolutePath());
                    String key = f.getName();
                    String value = f.getName().split("\\.")[0]; // we remove extension from the file name.

                    if (!al.containsKey(key) && Bridge.notIn(value, Bridge.FORBIDDEN_WORDS)) {
                        al.put(key, value);
                    }
                    if (!allFileList.containsKey(key)) {
                        allFileList.put(key, value);
                    }
                }
            }
            Bridge.files = new String[2][al.size()];
            SortedSet<String> sortedKeys = new TreeSet<String>(al.keySet());
            int i = 0;
            for (String key : sortedKeys) {
                Bridge.files[0][i] = key;
                Bridge.files[1][i] = al.get(key);
                i++;
            }
            Bridge.allFiles = new String[2][allFileList.size()];
            SortedSet<String> sortedKeysAllFiles = new TreeSet<String>(allFileList.keySet());
            int j = 0;
            for (String key : sortedKeysAllFiles) {
                Bridge.allFiles[0][j] = key;
                Bridge.allFiles[1][j] = allFileList.get(key);
                j++;
            }

        } else {
            System.out.println("The argument should be a directory ! Got : " + directory.getAbsolutePath());
        }
    }

    /**
     * This function checks if the <b>forbiddenWords</b> are contained within
     * the <b>stringToBeChecked</b>.
     *
     * @param stringToBeChecked The string to check
     * @param forbidenWords The array of forbidden words
     * @return true if the string is clear, false if it contains at least one
     * forbidden word.
     */
    public static boolean notIn(String stringToBeChecked, String[] forbiddenWords) {
        boolean clear = true;
        if (!stringToBeChecked.equals("")) {
            for (String s : forbiddenWords) {
                if (clear) {
                    clear = !stringToBeChecked.contains(s);
                }
            }
        } else {
            clear = false;
        }
        return clear;
    }

    /**
     * This function saves the String <b>j</b> into a file called
     * "./%ParticipantID%_1.csv".
     * <br>
     * It takes a formatted String containing data separated by underscores and
     * changes the underscores into commas. <br>
     * It appends this changed String to the end of the file. <br>
     * <br>
     * If the file does not exist, it creates it and adds the right header to it
     * (separation char ',' and the name of each columns : "Time" and
     * "Location"). <br>
     * If it exists and the data is the first one of the test (ie : if the time
     * is equal to 0), it skips a line to separate the tests. <br>
     * <br>
     * <i>If you want to specify a file path, please use the <b>saveJson(String
     * j, String filepath)</b> function.</i>
     *
     * @param j - the string to save
     */
    public void saveData(String j) {
        saveData(j, experimentId + "_1.csv");
    }

    /**
     * This function saves the String <b>j</b> into a file located by the
     * <b>filepath</b> parameter. <br>
     * It takes a formatted String containing data separated by underscores and
     * changes the underscores into commas. <br>
     * It appends this changed String to the end of the file. <br>
     * <br>
     * If the file does not exist, it creates it and adds the right header to it
     * (separation char ',' and the name of each columns : "Time" and
     * "Location"). <br>
     * If it exists and the data is the first one of the test, it adds one to
     * the file name to separate the tests <br>
     *
     * @param j - the string to save
     * @param filepath - the file path of the file to save in
     */
    public void saveData(String j, String filepath) {

        try {
            StringBuilder sb = new StringBuilder();

            File f;
            // we leave a space at the beginning of each test, to separate them
            if (this.firstStat) {
                int cpt = 1;
                new SimpleDateFormat("yyyyMMdd_HHmmss");
                Date date = new Date();
                f = new File(this.setup);

                if (!f.exists()) {
                    f.mkdir();
                }

                f = new File(this.setup + File.separator + filepath);
                while (f.exists()) {
                    filepath = incrementString(filepath);
                    f = new File(this.setup + File.separator + filepath);
                    cpt++;
                }

                this.fullFilepath = this.setup + File.separator + filepath;
                f.createNewFile();
                sb.append("sep=^");
                sb.append('\n');
                sb.append(date);
                sb.append('\n');
                sb.append("Experiment Id : " + experimentId);
                sb.append('\n');
                sb.append("Participant Id : " + cpt);
                sb.append('\n');
                sb.append("Setup: " + setup);
                sb.append('\n');
                sb.append("Training Time : " + tTime + " Final Quiz Time : " + fTime);
                sb.append('\n');

                sb.append("\n");
                this.firstStat = false;
            } else {

                f = new File(this.fullFilepath);
            }

            // add the data to the string to put in the file
            j = j.replace('_', '^');
            sb.append(j);
            sb.append('\n');

            FileWriter fw = new FileWriter(f, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            pw.write(sb.toString());
            pw.close();

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    public void print(String l) {
        System.out.println("quizplatform.Bridge.print()" + l);
    }

    /**
     * This function handles the callbacks from JS
     *
     * @param callback
     * @param function the called function of Bridge
     * @param args the args to a function
     */
    public void execute(Consumer<Object> callback, String function, Object... args) {
        callback.accept(window.call(function, args));
    }

    /**
     * Saves the changes in the html file of the bin directory so that the
     * changes are loaded the next time the page is loaded. Used to save the
     * highlighting (both add and remove).
     */
    public void savePage() {

        StringBuilder sb = new StringBuilder();

        File f = new File(this.previousUrl.replaceAll("src", "bin"));
        if (f.exists()) {
            this.changedHtml = this.changedHtml.replaceAll("(<div id=\"documents\">)[^&]*(</div>)", "$1 <h2>Documents</h2> $2");
            sb.append(this.changedHtml);
            FileWriter fw;
            try {
                fw = new FileWriter(f, false);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter pw = new PrintWriter(bw);
                pw.write(sb.toString());
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Use this to check if selected text is highlighted. Calls a javascript
     * function that does the check.
     */
    public void checkHighlight() {
        String highlightedText = this.engine.executeScript("checkHighlight()").toString();
        this.previousUrl = this.engine.getLocation().replace("file:///", "");
        this.changedHtml = (String) this.engine.executeScript("document.documentElement.outerHTML");
        getTime();
        this.getTrace(time + "_" + title + this.section + "_highlighted_" + highlightedText);
    }

    /**
     * Copies all the html files from the src directory to the bin directory,
     * thus resetting the highlighting
     */
    public void resetFiles() {
        Path source = Paths.get(this.srcPath);
        Path target = Paths.get(this.binPath);
        try {
            Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {

                private CopyOption options = REPLACE_EXISTING;

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {

                    Path targetdir = target.resolve(source.relativize(dir));
                    try {
                        if (!targetdir.toFile().exists() || !targetdir.toFile().isDirectory()) {
                            Files.copy(dir, targetdir, options);
                        }
                    } catch (FileAlreadyExistsException e) {
                        if (!Files.isDirectory(targetdir)) {
                            throw e;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.copy(file, target.resolve(source.relativize(file)), options);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the folder path where the HTML files are stored.
     *
     * @return a String containing the path of the folder
     */
    public String getDocumentsFolderPath() {

        String filePath = "./bin/quizplatform/html/" + this.setup + "/";
        return filePath;
    }

    /**
     * This function is called in JavaScript after checking/submitting the
     * answers of the final quiz. It stops and resets the timers. This is for
     * the scenario that a user submits the final quiz answers prior to the end
     * of the designated time.
     */
    public void submitFinalQuiz() {
        this.t.stop();
        this.demogTimer.restart();
    }

    /**
     * This function is responsible for managing the training and final quiz
     * time and redirecting us to the corresponding pages. Using the APIs
     * provided by the reactfx jar, after the first page load we creating timers
     * that after a period of time will manage the redirections or augment the
     * progressBar.
     *
     * @param progressBar the progressBar created in QuizPlatform.
     * @param step the step of the progression setting collected from the
     * execution parameters.
     */
    public void setTimersAndProgressBar(ProgressBar progressBar, float step, float tTime, float fTime) {
        augmentBar = ((tTime / step));
        Timer timer = FxTimer.runPeriodically(
                Duration.ofMillis((long) (augmentBar * MILIS)),
                () -> {
                    percent += 1 / step;
                    progressBar.setProgress(percent);
                });

        FxTimer.runLater(
                Duration.ofMillis((long) ((tTime * MILIS) + 3000)), // adds 3 seconds to the time so that the progress bar is full during 3 seconds
                () -> {
                    percent = 0;
                    augmentBar = ((fTime / step));
                    timer.stop();
                    timer2 = FxTimer.runPeriodically(
                            Duration.ofMillis((long) (augmentBar * MILIS)),
                            () -> {
                                percent += 1 / step;
                                progressBar.setProgress(percent);
                            });

                    progressBar.setProgress(percent);
                    this.t.restart();
                    engine.load(getClass().getResource(binPath.substring(1) + "/final_quiz.html").toExternalForm());
                });

        this.demogTimer = FxTimer.create(
                Duration.ofMillis((long) (1)),
                () -> {
                    timer2.stop();
                    percent = 0;
                    progressBar.setProgress(percent);
                    engine.load(getClass().getResource(binPath.substring(1) + "/info.html").toExternalForm());
                });

        this.t = FxTimer.create(
                Duration.ofMillis((long) ((fTime) * MILIS)),
                () -> {
                    engine.executeScript("checkFinalAnswers();");
                });
    }

}
