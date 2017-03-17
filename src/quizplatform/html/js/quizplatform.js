var startDate = new Date();
var start=startDate.getTime();
var seconds=1;
var time;

function updatePageTime(timeJ){
    time=timeJ;
}

function updateJavaTime(){
    var exitTime=timer()+time;
    java.updateTime(exitTime);
}

function showText(text) {
    document.getElementById("text").innerHTML = text;
    return text;
}

function sendJson(){

    var pageName=document.title;
    
    var s = time.toString()+"_"+pageName.toString();
    java.getJson(s);
    updateJavaTime();

 }

function quit(){
    var pageName=document.title;
    var exitTime=time+timer();
    var s = time.toString()+"_Exit"+pageName.toString();
    java.getJson(s);
    java.exit();
}

function timer(){
    var end=new Date();
    seconds=(end.getTime() - start) ;
    return seconds;
}

function sendElementJson(){
    
    
    var id = $('#accordion .in').parent().attr("id");
    var pageName=document.title+"-"+id;
    var s = time.toString()+"_"+pageName.toString();

    java.getJson(s);
    updateJavaTime();
    
}
