var qUrl;

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


function exitQuiz() {



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

function sendUrl() {
    var url = window.location.pathname;
    java.getUrl(url);
}


function setQuizUrl() {

    var a = document.getElementById('quiz_start'); //or grab it by tagname etc
    a.href = qUrl;

    if (qUrl === '#') {
        messageDivCompl = document.getElementById('message_completed');
        messageDivCompl.style.color = "green";
        messageDivCompl.innerHTML = 'You have completed the quiz';
        return;

    }


}


function backToDoc() {

    var b = document.getElementById('back'); //or grab it by tagname etc
    b.href = bUrl;
    sendTrace();

}


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