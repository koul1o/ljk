package quizplatform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
/**
 *
 * @author koul1o
 */
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class Bridge {

    private int time;
    private JSObject window ;
    private String title;
    private WebEngine engine;
    String docUrl = null;
    
    HashMap<String, String> quizLinks;
    
    public Bridge(WebEngine engine,Stage stage) {
        time=0;
        this.quizLinks = new HashMap<String, String>();
        engine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> obs, State oldState, State newState) -> {
            if (newState == State.SUCCEEDED) { 
                       
                       this.engine=engine;
                       window = (JSObject) engine.executeScript("window");
                       /* Register our java app to the window so that we can make upcalls to the app using java.functionName(); */
                       window.setMember("java", this);
                       title=engine.getTitle();
                       stage.setTitle(engine.getTitle());
                       /* */
                        if (engine != null) 
                            {
                                /* Update the global time passed everytime we load a new page */ 
                                engine.executeScript("var time="+time+"");
                                /* Check if we are in a document page and format the url removing the file:// prefix */ 
                                if(engine.getTitle().toLowerCase().contains("document ")){
                                    docUrl=engine.getLocation();
                                    docUrl=docUrl.replace("file://","");
                                }
                                /* Update the doc url in the webpage */
                                if (docUrl!=null){
                                    engine.executeScript("var bUrl=\'"+docUrl+"\'"+"");
                                    
                                    // add the doc into the hashmap if it doesn't exist yet then update the quiz URL
                                    if(!quizLinks.containsKey(docUrl)){
                                    	quizLinks.put(docUrl, docUrl.replace(".html", "_quiz1.html"));
                                    }
                                    
                                    /* 	if the quizLink point to a quiz (ie if the quiz hasn't already been finished) it changes the value of qUrl
                                    	the next question of the quizz */
                                    
                                    if(quizLinks.get(docUrl).contains("_quiz")){
                                    	engine.executeScript("var qUrl=\'" + quizLinks.get(docUrl) + "\'");
                                        System.out.println("qurl"+quizLinks.get(docUrl));
                                    } else {
                                    	engine.executeScript("var qUrl='#'");
                                    }
                                    
                                    System.out.println(quizLinks);
                                }
                               
                            }
                    }
            });
    }
    
    
    /* Upcall to this function from the page, to update the global time passed  */
    public void updateTime(int time){
        this.time=time;
        //System.out.println("Exit time: "+time);
    }
    
    /* Function, to exit the platform */
    public void exit() {
        Platform.exit();
        
    }
    
    /* Upcall to this function from the page, to get the interaction trace */
    public void getTrace(String j){
        System.out.println("Trace: "+j);
        saveData(j);
        
    }
    
    /* Upcall to this function from the page, to update the next question Url for a document quiz */
    public void getUrl(String url){
        
        System.out.println("quizplatform.Bridge.getUrl()" +url);
        URLToNextQuestion(url);
        redirect(quizLinks.get(docUrl));
    }
    
    public void URLToNextQuestion(String quizUrl){

    	Pattern digitPattern = Pattern.compile("(\\d+)");

    	Matcher matcher = digitPattern.matcher(quizUrl);
    	StringBuffer result = new StringBuffer();
    	int index = 0;
    	while (matcher.find())
    	{

        	System.out.println("Next q : " + result.toString());
        	index = matcher.start();
        	
        }
    	System.out.println("index = " + index);
    	matcher.find(index);
    	matcher.appendReplacement(result, String.valueOf(Integer.parseInt(matcher.group(1)) + 1));
    	matcher.appendTail(result);

    	String r = result.toString();

    	System.out.println("Next q : " + result.toString());
    	File f = new File(r);
    	if(!f.exists()){
    		String s[] = r.split("/");
    		r = "";
    		s[s.length-1] = "documents.html";
    		int i = 0;
    		for(i=0; i<s.length; i++){
    			if(i != 0){
    				r = r + "/" + s[i];
    			} else {
        			r = r + s[i];
    			}
    		}
    		
    		System.out.println(r);
    	}
    	
		quizLinks.replace(docUrl, r);
                System.out.println("quizplatform.Bridge.URLToNextQuestion()"+r);
                System.out.println("quizplatform.Bridge.URLToNextQuestion()"+quizLinks);
    	
    }
    
    /* This function redirects us to the next question while in the quiz */
    public void redirect (String url){
        System.out.println("quizplatform.Bridge.redirect() "+url);
        //engine.load(getClass().getResource("html/document_page.html").toExternalForm());
        engine.executeScript("window.location.replace(\'" + url + "\');");
        //engine.executeScript("redirect();");
    }
    
    /**
     * This function saves the String <b>j</b> into a file called "./test.csv". <br>
     * It takes a formatted String containing data separated by underscores and changes the underscores into commas. <br>
     * It appends this changed String to the end of the file. <br>
     * <br>
     * If the file does not exist, it creates it and adds the right header to it (separation char ',' and the name of each columns : "Time" and "Location"). <br>
     * If it exists and the data is the first one of the test (ie : if the time is equal to 0), it skips a line to separate the tests. <br>
     * <br>
     * <i>If you want to specify a file path, please use the <b>saveJson(String j, String filepath)</b> function.</i>
     * 
     * @param j - the string to save
     */
    
    public void saveData(String j){
    	saveData(j, "test.csv");
    }
    
    /**
     * This function saves the String <b>j</b> into a file located by the <b>filepath</b> parameter. <br>
     * It takes a formatted String containing data separated by underscores and changes the underscores into commas. <br>
     * It appends this changed String to the end of the file. <br>
     * <br>
     * If the file does not exist, it creates it and adds the right header to it (separation char ',' and the name of each columns : "Time" and "Location"). <br>
     * If it exists and the data is the first one of the test (ie : if the time is equal to 0), it skips a line to separate the tests. <br>
     * 
     * @param j - the string to save
     * @param filepath - the file path of the file to save in 
     */
    
    public void saveData(String j, String filepath){
    	
    	try {
    		StringBuilder sb = new StringBuilder();
    		File f = new File(filepath);
    		
    		// if the file doesn't exist we need to create it and add the header (separation char and name of the columns)
    		if(!f.exists()){
    			f.createNewFile();
    			sb.append("sep=,");
        		sb.append('\n');
    			sb.append("Time,Location");
        		sb.append('\n');
    		}
    		
    		// we leave a space at the beginning of each test, to separate them
    		if(j.startsWith("0")){
    			sb.append("\n");
    		}
    		
    		// add the data to the string to put in the file
    		j = j.replace('_', ',');
    		sb.append(j);
    		sb.append('\n');
    		
    		FileWriter fw = new FileWriter(f,true);
    		BufferedWriter bw = new BufferedWriter(fw);
      	  	PrintWriter pw = new PrintWriter(bw);

			pw.write(sb.toString());
			pw.close();

		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		} catch (IOException e){
			
			e.printStackTrace();
			
		}
    	
    }
    

    public void execute(Consumer<Object> callback, String function, Object... args) {
        callback.accept(window.call(function, args));
    }
}
