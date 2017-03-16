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
    var jsonObj = {
    
                time:time,page:pageName

    }

    var s = time.toString()+"_"+pageName.toString();
    java.getJson(s);
    updateJavaTime();

 }

function quit(){
    var pageName=document.title;
    var exitTime=time+timer();
    var jsonObj = {
   
                time:exitTime,page:"Exit "+pageName

    }

    var s=JSON.stringify(jsonObj);
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
    var pageName=document.title+"_"+id;
    var jsonObj = {
    sequence: 
           {
                time:time,page:pageName


        }
    }

    var s=JSON.stringify(jsonObj);
    java.getJson(s);
    updateJavaTime();
    
}
