package quizplatform;

/**
 *
 * @author koul1o
 */


import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class Bridge {

	private static final String QUESTION_NAME = "question";
	private static final String DOCUMENT_PATH = "../../git/ljk/src/quizplatform/html";
	private static final String[] FORBIDDEN_WORDS = {QUESTION_NAME, "start2", "final_quiz", "manual", "documents"};
	
    private int time;
    private JSObject window ;
    private String title;
    private WebEngine engine;
    String docUrl = null;
    
    final LongProperty startTime = new SimpleLongProperty();
    final LongProperty endTime = new SimpleLongProperty();
    final LongProperty elapsedTime = new SimpleLongProperty();
    
    private int cnt ,cnt2 = 1;
    
    HashMap<String, String> quizLinks;
    boolean firstStat = true;
    
    public Bridge(WebEngine engine,Stage stage) {
        time=0;
        this.quizLinks = new HashMap<String, String>();
        engine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> obs, State oldState, State newState) -> {
    	switch (newState) {
	        case RUNNING:
	            startTime.set(System.nanoTime());
	            break;
	
	        case SUCCEEDED:
	            endTime.set(System.nanoTime());
	            elapsedTime.bind(Bindings.subtract(endTime, startTime));
	            if (cnt2>0){
	            	System.out.println(time);
	                time=(int) (time+elapsedTime.divide(1_000_000).getValue());
	                System.out.println("Time: "+time+" Elapsed t: "+elapsedTime.divide(1_000_000).getValue() );
	            }
	            cnt2++;
	            break;
    	}
        	
        	if (newState == State.SUCCEEDED) { 
                       
                       this.engine=engine;
                       window = (JSObject) engine.executeScript("window");
                       /* Register our java app to the window so that we can make upcalls to the app using java.functionName(); */
                       window.setMember("java", this);
                       title=engine.getTitle();
                       stage.setTitle(engine.getTitle());
                       /* */
                       try {
                    	   findFiles(new File(Bridge.DOCUMENT_PATH));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        if (engine != null) 
                            {
                                /* Update the global time passed everytime we load a new page */ 
                                engine.executeScript("var time="+time+"");
                                /* Check if we are in a document page and format the url removing the file:// prefix */ 
                                if(engine.getTitle().toLowerCase().contains("document ") && !title.toLowerCase().contains(Bridge.QUESTION_NAME)){
                                    docUrl=engine.getLocation();
                                    docUrl=docUrl.replace("file://","");
                                }
                                /* Update the doc url in the webpage */
                                if (docUrl!=null){
                                    engine.executeScript("var bUrl=\'"+docUrl+"\'"+"");
                                    
                                                                      
                                    // add the doc into the hashmap if it doesn't exist yet then update the quiz URL
                                    if(!this.quizLinks.containsKey(docUrl) && !title.toLowerCase().contains(Bridge.QUESTION_NAME)){
                                    	this.quizLinks.put(docUrl, docUrl.replace(".html", "_"+Bridge.QUESTION_NAME+"1.html"));
                                    }
                                    
                                    /* 	if the quizLink point to a quiz (ie if the quiz hasn't already been finished) it changes the value of qUrl
                                    	the next question of the quizz */
                                    
                                    if(this.quizLinks.get(docUrl) != null && this.quizLinks.get(docUrl).contains("_"+Bridge.QUESTION_NAME)){
                                    	engine.executeScript("var qUrl=\'" + this.quizLinks.get(docUrl) + "\'");
                                    } else {
                                    	engine.executeScript("var qUrl='#'");
                                    }
                                    
                                }
                                
                                 if(title.toLowerCase().contains(Bridge.QUESTION_NAME)){
                                        engine.executeScript("sendTrace()");
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
        //lastTrace();
        Platform.exit();
        
    }
    
    /* Upcall to this function from the page, to get the interaction trace */
    public void getTrace(String j){
        System.out.println("Trace: "+j);
        saveData(j);
        
    }
    /* Upcall to this function from the page, to get the last trace on window exit */
    public void lastTrace(){
        engine.executeScript("sendTrace();quit();");
    }
    public void finalQuiz(){
        engine.executeScript("sendTrace();finalQuiz();");
    }
    
    /* Upcall to this function from the page, to update the next question Url for a document quiz */
    public void getUrl(String url){
        URLToNextQuestion(url);
        redirect(this.quizLinks.get(docUrl));
    }
    
    /**
     * This function changes the String <b>quizUrl</b> by adding 1 to the number of the quiz. So C://example/document1_quiz1.html would become C://example/document1_quiz2.html.<br>
     * It takes the path to the html file then parses it and changes only the last number. <br>
     * It then changes the entry of the document in the <b>quizLinks</b> hashmap so that the value matches the path of the next question. <br>
     * <br>
     * If the resulting file does not exist, the url is set to the list of documents. <br>
     * 
     * @param quizUrl - the url to save
     */
    
    public void URLToNextQuestion(String quizUrl){

    	Pattern digitPattern = Pattern.compile("(\\d+)");

    	Matcher matcher = digitPattern.matcher(quizUrl);
    	StringBuffer result = new StringBuffer();
    	int index = 0;
    	while (matcher.find())
    	{
        	index = matcher.start();      	
        }
    	matcher.find(index);
    	matcher.appendReplacement(result, String.valueOf(Integer.parseInt(matcher.group(1)) + 1));
    	matcher.appendTail(result);

    	String r = result.toString();
        
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
    		
    	}
    	
		this.quizLinks.replace(docUrl, r);
                
    }
    
    /* This function redirects us to the next question while in the quiz */
    public void redirect (String url){
           engine.executeScript("window.location.replace(\'" + url + "\');");
    }
    
    /**
     * This function goes through all files contained in the <b>directory</b> path. If the file is a document, then it adds it to the returning array.<br/>
     * The entries of the array have two values, the first one is the canonical path to the file and the second is the name of the file without its extension.<br/>
     * 
     * @param directory The path to the directory to explore
     * @return An array of two dimensions. The first dimension contains the canonical path (0) and the filename (1), the second dimension is the entries
     * @throws IOException If an I/O error occurs, which is possible because the construction of the canonical pathname may require filesystem queries.
     */
    
    public static String[][] findFiles(File directory) throws IOException {
    	File[] file;
    	HashMap<String, String> al = new HashMap<String, String>();
        if (directory.isDirectory()) {
            file = directory.listFiles(); // Calls same method again.
            for(File f : file){
            	if(f.isDirectory()){
            		// findFiles(f);
            	} else {
            		String key = f.getCanonicalPath();
            		//TODO break down the string to obtain the name of the document only (without the extension and the path) and set it as value of entry
            		String value = f.getName().split("\\.")[0]; // we remove extension from the file name.
            		
            		if(!al.containsKey(key) && notIn(value, Bridge.FORBIDDEN_WORDS)){
            			al.put(key, value);
    					System.out.println(f.getName() + " / " + value);
            		}
            	}
            }
            String[][] result = new String[2][al.size()];
            int i = 0;
            for(String key : al.keySet()){
            	result[0][i] = key;
            	result[1][i] = al.get(key);
            	System.out.println(result[0][i] + " / " + result[1][i]);
            	i++;
            }
            return result;
        } else {
            System.out.println("The argument should be a directory ! Got : " + directory.getAbsolutePath());
        }
        return null;
    }
    
    /**
     * This function checks if the <b>forbiddenWords</b> are contained within the <b>stringToBeChecked</b>.
     * 
     * @param stringToBeChecked The string to check
     * @param forbidenWords The array of forbidden words
     * @return true if the string is clear, false if it contains at least one forbidden word.
     */
    
    public static boolean notIn(String stringToBeChecked, String[] forbiddenWords){
    	boolean clear = true;
    	if(!stringToBeChecked.equals("")){
	    	for(String s : forbiddenWords){
	    		if(clear){
	    			clear = !stringToBeChecked.contains(s);
	    		}
	    	}
    	} else {
    		clear = false;
    	}
    	
    	return clear;
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
    		if(this.firstStat){
    			sb.append("\n");
    			this.firstStat = false;
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
