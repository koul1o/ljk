var qUrl;
var docs;

/** 
 * Upcall to Java sending the time and the name of the accesed page for the final page 
 */
function quit() {
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
    var id = event.srcElement.name+"_"+event.srcElement.id;
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
    retakeBtn=document.getElementById('retake');
    retakeBtn.style.display = "";
    messageDivCompl.style.display = "";
    document.getElementById("quiz-container").style.height = "10px";
    
    
}
function retake(){
    java.restartQuiz();
    
    document.getElementById("mbd").style.display = "";
     messageDivCompl = document.getElementById('message_completed');
    retakeBtn=document.getElementById('retake');
    retakeBtn.style.display = "none";
    messageDivCompl.style.display = "none";
    document.getElementById("quiz-container").style.height = "650px";
    
    redirect();
    
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
            ans = radios[i].name + "_Answer: " + radios[i].value;
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

        if (!info[i].value ) {
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

function checkHighlight(){
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

function unHighlight(){
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

function clearHighlights(){
	// select element to unwrap
	var el = document.querySelector('#highlighted');
	
	// get the element's parent node
	if(el != null){
		var parent = el.parentNode;
		
		// move all children out of the element
		while (el.firstChild) parent.insertBefore(el.firstChild, el);
		
		// remove the empty element
		parent.removeChild(el);
	}
}
