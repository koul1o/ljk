var startDate = new Date();
var start=startDate.getTime();
var ms=1;
var time;

/**
 * Update the time passed since the intit of the platform
 * Function called from Class Bridge everytime we access a new page 
 */
function updatePageTime(timeJ){
    time=timeJ;
}

/** 
  * Upcall to Java to update the time passed since the intit of the platform
  */
function updateJavaTime(){
    var exitTime=timer()+time;
    java.updateTime(exitTime);
}

/** 
  *Upcall to Java sending the time and the name of accessed page
  */
function sendTrace(){

    var pageName=document.title;
    var s = time.toString()+"_"+pageName.toString();
    java.getTrace(s);
    updateJavaTime();

 }

/** 
  * Upcall to Java sending the time and the name of the accesed page for the final page 
  */
function quit(){
    var pageName=document.title;
    var exitTime=time+timer();
    var s = exitTime.toString()+"_Exit"+pageName.toString();
    java.getTrace(s);
    java.exit();
}

/** 
  * Calculate the time passed in a page in ms
  */
function timer(){
    var end=new Date();
    ms=(end.getTime() - start) ;
    return ms;
}

/** 
  * Upcall to Java sending the time and the page accessed in a dom element
  */
function sendElementTrace(){
    
    
    var id = $('#accordion .in').parent().attr("id");
    var pageName=document.title+"-"+id;
    var s = time.toString()+"_"+pageName.toString();
    java.getTrace(s);
    updateJavaTime();
    
}
