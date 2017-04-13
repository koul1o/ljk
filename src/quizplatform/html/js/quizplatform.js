var qUrl;
var docs;

/** 
 * Upcall to Java sending the time and the name of the accesed page for the final page 
 */
function quit() {
    var pageName = document.title;
    var exitTime = time + timer();
    var s = exitTime.toString() + "_" + pageName.toString() + "_Platform Exit";
    java.getTrace(s);
    java.exit();
}

/** 
 * Upcall to Java sending the time and the page accessed in a dom element
 */
function sendElementTrace() {
    // var id = $('#accordion .in').parent().attr("id");
    var id = event.srcElement.parentNode.parentNode.parentNode.id;
    java.elementTrace(id);

}

function sendElementTraceQ() {
    // var id = $('#accordion .in').parent().attr("id");
    var id = event.srcElement.id;
    java.elementTrace(id);

}

function iframeElementTrace(trace) {
    java.elementTrace(trace);
}

function iframeElementTraceCorrect(trace) {
    java.elementTrace(trace);
}

/*
 function checkAnswer() {
 var message = 'Try again',
 selected = document.querySelector('input[value="correct"]:checked'),
 messageDiv = document.querySelector('#message');
 messageDiv.style.color = "red";
 
 var radios = document.getElementsByClassName("question_item");
 var ans;
 for (var i = 0, length = radios.length; i < length; i++) {
 if (radios[i].checked) {
 ans = radios[i].value;
 }
 }
 
 
 if (selected) {
 
 
 message = 'Correct';
 messageDiv.style.color = "green";
 
 sendUrl();
 }
 var pageName = document.title;
 var s = "_Answer: " + ans;
 java.elementTrace(s);
 messageDiv.innerHTML = message;
 
 }
 */

function sendUrl() {
    var url = qUrl;
    java.getUrl(url);
}


function setQuizUrl() {

    document.getElementById("mbd").src = qUrl;

    if (qUrl === '#') {
        afterSubmit();
    }


}

function afterSubmit() {
    document.getElementById("mbd").style.display = "none";
    messageDivCompl = document.getElementById('message_completed');
    messageDivCompl.style.display = "";
    document.getElementById("quiz-container").style.height = "10px";
}


/*
 function backToDoc() {
 
 var b = document.getElementById('back'); //or grab it by tagname etc
 b.href = bUrl;
 sendTrace();
 
 }
 */

function checkFinalAnswers() {
    var radios = document.getElementsByClassName("question_item");
    var ans;
    for (var i = 0, length = radios.length; i < length; i++) {
        if (radios[i].checked) {
            ans = "_Answer: " + radios[i].value;
            java.elementTrace(ans);

        }
    }
}

function redirect() {

    document.getElementById("mbd").src = qUrl;
    document.getElementById('mbd').contentWindow.reload();

}

function qTrace() {

    title = "" + document.getElementById("mbd").contentDocument.title;
    java.elementTrace(title);
}

function setDocuments() {
    var divDoc = document.getElementById("documents");

    for (var i = 0; i < docs.length; i++)
        divDoc.innerHTML += "<ul><a href=\'" + docs[i][0] + "\'>" + docs[i][1] + "</a></ul>";
}


function print() {

    java.print(docs.length);
}