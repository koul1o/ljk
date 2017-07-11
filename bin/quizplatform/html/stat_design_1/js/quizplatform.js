var qUrl;
var docs;

/** 
 * Upcall to Java at the final page, to quit the platform (and save the last trace in Bridge). 
 */
function quit() {
    java.exit();
}

/** 
 * Upcall to Java sending the id of a dom element - here, Panels
 */
function sendElementTrace() {
    var id = event.srcElement.parentNode.parentNode.parentNode.id;
    java.elementTrace(id);

}

/** 
 * Upcall to Java sending the id of a dom element - here, Questions
 */
function sendElementTraceQ() {
    var id = event.srcElement.name + "_" + event.srcElement.id;
    java.elementTrace(id);

}

/** 
 * Upcall to Java sending a trace from the quiz iframe, here just the Page title & the Question title(iframe). 
 * The function is called by quizplatfrom.iframe.js
 */
function iframeElementTrace(trace) {
    java.elementTrace(trace);
}

/** 
 * Upcall to Java sending the current qUrl (url of the current question). 
 */
function sendUrl() {
    var url = qUrl;
    java.getUrl(url);
}

/** 
 * This function, once we clicked on the quiz panel sets the src of the iframe containing the quiz. 
 * If the quiz is copleted (qUrl == #) afterSubmit() is called.  
 */
function setQuizUrl() {
    document.getElementById("mbd").src = qUrl;
    if (qUrl === '#') {
        afterSubmit();
    }
}

/** 
 * This function, once called, hides the iframe and dissplays the retake button 
 * and the retake message. These elements are hidden by default (.style.display="none").   
 */
function afterSubmit() {
    document.getElementById("mbd").style.display = "none";
    messageDivCompl = document.getElementById('message_completed');
    retakeBtn = document.getElementById('retake');
    retakeBtn.style.display = "";
    messageDivCompl.style.display = "";
    document.getElementById("quiz-container").style.height = "10px";


}

/** 
 * This function, makes an upcall to Java to restart the quiz URLs.
 * Then displays the quiz iframe and hides the retake button and message.
 * Finaly it calls redirect() to load the correct url in the iframe. 
 */
function retake() {
    java.restartQuiz();
    document.getElementById("mbd").style.display = "";
    messageDivCompl = document.getElementById('message_completed');
    retakeBtn = document.getElementById('retake');
    retakeBtn.style.display = "none";
    messageDivCompl.style.display = "none";
    document.getElementById("quiz-container").style.height = "650px";
    redirect();

}


/** 
 * This function, collects all the selected answers of the final quiz. 
 * It makes an upcall to Java to sent the collected traces (Question 1_Answer_c) 
 * and a second upcall to reset the timers after all traces were collected.
 */
function checkFinalAnswers() {
    var radios = document.getElementsByClassName("question_item");
    var ans;
    for (var i = 0, length = radios.length; i < length; i++) {
        if (radios[i].checked) {
            ans = radios[i].name + "_Answer: " + radios[i].value;
            java.elementTrace(ans);
        }

    }
    java.submitFinalQuiz();
}

/** 
 * This function, is responsible for loading the questions in the quiz iframe.
 * It sets the quiz ifram src to qUrl and then reloads the window.
 */
function redirect() {

    document.getElementById("mbd").src = qUrl;
    document.getElementById('mbd').contentWindow.reload();

}

/** 
 * Upcall to Java sending the quiz iframe's title trace. 
 */
function qTrace() {

    title = "" + document.getElementById("mbd").contentDocument.title;
    java.elementTrace(title);
}

/** 
 * This function is setting the document page URLs and titles.
 * The fucntion is called by class Bridge when entering the documents.html page.  
 */
function setDocuments() {
    var divDoc = document.getElementById("documents");
    for (var i = 0; i < docs.length; i++)
        divDoc.innerHTML += "<ul><a href=\'" + docs[i][0] + "\'>" + docs[i][1] + "</a></ul>";
}

/** 
 * Just a print function to print into console by upcalling the print function of Bridge.
 */
function print() {
    java.print(docs.length);
}

/** 
 * This function, is called after submiting the demographic information at info.html.
 * It collects all the fields and performs upcalls to Java to save them as traces.
 * If certain fields are not filled it displays a message to the user. (message.style.display = "";)
 * Initialy the message is hidden (message.style.display = "none";)
 * Finaly it calls quit()to exit the platform.
 */
function collectInfo() {

    var message = document.getElementById('message');
    var info = document.getElementsByClassName('form-control');
    var infoRadio = document.getElementsByClassName('form-control-radio');
    var ans;
    for (var i = 0, length = infoRadio.length; i < length; i++) {
        if (infoRadio[i].checked) {
            ans = infoRadio[i].name + ": " + infoRadio[i].value;
            java.elementTrace(ans);
        }
    }
    for (var i = 0, length = info.length; i < length; i++) {

        if (!info[i].value) {
            java.print(i);
            message.style.display = "";
            return;
        }
    }
    for (var i = 0, length = info.length; i < length; i++) {
        ans = info[i].id + ": " + info[i].value;
        java.elementTrace(ans);
    }
    quit();
}


/** 
 * This function, takes the selected words or sentences and checks wether they are already highlighted.
 * If highlighted calls unhighlight() if not calls highlight() and returns the selected string.
 */
function checkHighlight() {
    if (window.getSelection) {
        var selection = window.getSelection();
        var selectionValue = selection.toString();
        if (selection.rangeCount) {
            var range = selection.getRangeAt(0).cloneRange();
            var node = $(range.commonAncestorContainer);
            if (node.parent().is("span")) { // && node.parent().id == "highlighted"
                unHighlight();
            } else {
                highlight();
                return selectionValue;
            }
        }
    }
}

/** 
 * This function, is called to highlight a word or sentence.
 */
function highlight() {
    if (window.getSelection) {
        var selection = window.getSelection();
        if (selection.rangeCount) {
            var range = selection.getRangeAt(0).cloneRange();
            var highlightNode = document.createElement("span");
            highlightNode.setAttribute("id", "highlighted");
            highlightNode.setAttribute("style", "background-color:#FFFF00");
            range.surroundContents(highlightNode);
            selection.removeAllRanges();
        }
    }
}

/** 
 * This function, is called to unhighlight highlighted section.
 */
function unHighlight() {
    if (window.getSelection) {
        var selection = window.getSelection();
        if (selection.rangeCount) {

            var highlightNode = document.createElement("span");
            highlightNode.setAttribute("id", "highlighted");
            highlightNode.setAttribute("style", "background-color:#FFFF00");

            var range = selection.getRangeAt(0).cloneRange();
            var node = $(range.commonAncestorContainer);

            var previousRange = document.createRange();
            previousRange.setStart(range.startContainer, 0);
            previousRange.setEnd(range.startContainer, range.startOffset);

            var nextRange = document.createRange();
            nextRange.setStart(range.endContainer, range.endOffset);
            nextRange.setEnd(range.endContainer, 0);

            node.unwrap();
            previousRange.surroundContents(highlightNode);
            nextRange.surroundContents(highlightNode);

            selection.removeAllRanges();
            selection.addRange(previousRange);
            selection.addRange(nextRange);
        }
    }
}

/** 
 * This function, is called to clean the highlights.
 */
function clearHighlights() {
    // select element to unwrap
    var el = document.querySelector('#highlighted');

    // get the element's parent node
    if (el !== null) {
        var parent = el.parentNode;

        // move all children out of the element
        while (el.firstChild)
            parent.insertBefore(el.firstChild, el);

        // remove the empty element
        parent.removeChild(el);
    }
}
