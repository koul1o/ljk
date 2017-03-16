package quizplatform;

/**
 *
 * @author koul1o
 */
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class Bridge {

    private int time;
    private JSObject window ;
    private String title;
    public Bridge(WebEngine engine,Stage stage) {
        time=0;
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {                        
                       window = (JSObject) engine.executeScript("window");
                       window.setMember("java", this);
                       title=engine.getTitle();
                       stage.setTitle(engine.getTitle());
                        if (engine != null) 
                            {
                               engine.executeScript("var time="+time+"");
                            }
                    }
            });
    }
    
    
    
    public void updateTime(int time){
        this.time=time;
        System.out.println("Exit time: "+time);
    }
    
    
    public void exit() {
        Platform.exit();
        
    }
   
    
    public String sendTitle(){
        
        return title;
    }
    
    public void getJson(String j){
       
        
        String pageName = j.toString();
        System.out.println("JSON object: "+j);
        
    }
    

    public void execute(Consumer<Object> callback, String function, Object... args) {
        callback.accept(window.call(function, args));
    }
}
