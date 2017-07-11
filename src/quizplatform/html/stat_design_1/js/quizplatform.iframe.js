var pageName = document.title;


/** 
 * This function sends traces when changing our selection
 * (from option a to option b) by calling the iframeElementTrace() of quizplatform.js. 
 */
function sendElementTraceQ() {
    var id = pageName + "_" + event.srcElement.id;
    window.parent.iframeElementTrace(id);
}


/** 
 * This function checks the submited answer and if correct, sends sends traces
 * by calling the iframeElementTrace() and sendUrl() (to proceed to next question) of quizplatform.js.
 * If the submited answer is wrong, it displays a message to try again (messageDiv.innerHTML = message). 
 * 
 */
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
        var s = pageName + "_Answer: " + ans;
        window.parent.iframeElementTrace(s);
        window.parent.sendUrl();
    }

    var s = pageName + "_Answer: " + ans;
    window.parent.iframeElementTrace(s);
    messageDiv.innerHTML = message;
}
