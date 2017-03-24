package quizplatform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
/**
 *
 * @author koul1o
 */
import java.util.function.Consumer;
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
    public Bridge(WebEngine engine,Stage stage) {
        time=0;
        
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
                                }
                               
                                
                                /* Update the starting question of a quiz when inside a document page */
                                /*
                                for (int i = 0; i < 10; i++) {
                                    if (==engine.getLocation()){
                                        engine.executeScript("var qUrl='/C:/Users/koul1o/Workspaces/Netbeans/QuizPlatform/build/classes/quizplatform/html/quiz1.html\'");
                                    }
                                }
                                */
                                engine.executeScript("var qUrl='/C:/Users/koul1o/Workspaces/Netbeans/QuizPlatform/build/classes/quizplatform/html/quiz1.html\'");
                                
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
        saveJson(j);
        
    }
    
    /* Upcall to this function from the page, to update the next question Url for a document quiz */
    public void getUrl(String url){
        
        System.out.println("quizplatform.Bridge.getUrl()" +url);
        redirect();
    }
    
    /* This function redirects us to the next question while in the quiz */
    public void redirect (){
        
        //engine.load(getClass().getResource("html/document_page.html").toExternalForm());
        engine.executeScript("window.location.replace(\'/C:/Users/koul1o/Workspaces/Netbeans/QuizPlatform/build/classes/quizplatform/html/quiz2.html\');");
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
    
    public void saveJson(String j){
    	saveJson(j, "test.csv");
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
    
    public void saveJson(String j, String filepath){
    	
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
