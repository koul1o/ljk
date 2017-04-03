var startDate = new Date();
var start=startDate.getTime();
var ms=1;
var time;
var qUrl;

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
    var s = exitTime.toString()+"_"+pageName.toString()+"_Platform Exit";
    java.getTrace(s);
    java.exit();
}


function finalQuiz(){
    var pageName=document.title;
    var exitTime=time+timer();
    var s = exitTime.toString()+"_"+pageName.toString();
    updateJavaTime();
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
    var pageName=document.title+" - "+id;
    var s = time.toString()+"_"+pageName.toString();
    java.getTrace(s);
    updateJavaTime();
    
}

function checkAnswer() {
  var message= 'Try again',
      selected= document.querySelector('input[value="correct"]:checked'),
      messageDiv= document.querySelector('#message');
      messageDiv.style.color="red";

    var radios = document.getElementsByClassName("question_item");
    var ans;
    for (var i = 0, length = radios.length; i < length; i++) {
        if (radios[i].checked) {
           ans=radios[i].value;
        }
    }
   
  
  if(selected) {
  	
   
    message='Correct';
    messageDiv.style.color="green";
    sendUrl();
  }
  var pageName=document.title;
  var s = time.toString()+"_"+pageName.toString()+"_Answer: "+ans;
  java.getTrace(s);
  updateJavaTime();
 
  messageDiv.innerHTML=message;
}

function sendUrl(){
var url=window.location.pathname;
java.getUrl(url);
}


function setQuizUrl(){
    
    var a = document.getElementById('quiz_start'); //or grab it by tagname etc
    a.href = qUrl;
    
  if(qUrl==='#'){
    messageDivCompl=document.getElementById('message_completed') ;
    messageDivCompl.style.color="green";
    messageDivCompl.innerHTML= 'You have completed the quiz';
    return;
    
  }
    sendElementTrace();
    
}


function backToDoc(){
    
    var b = document.getElementById('back'); //or grab it by tagname etc
    b.href = bUrl;
    sendTrace();
    
}


function checkFinalAnswers(){
var radios = document.getElementsByClassName("question_item");
var ans;
    for (var i = 0, length = radios.length; i < length; i++) {
        if (radios[i].checked) {
            ans=radios[i].name+"_Answer: "+radios[i].value;
            java.getTrace(ans);
           
        }
    }
}