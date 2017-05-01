var pageName = document.title;

function sendElementTraceQ() {
    // var id = $('#accordion .in').parent().attr("id");
    var id = pageName + "_" + event.srcElement.id;
    window.parent.iframeElementTrace(id);
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
        var s = pageName + "_Answer: " + ans;
        window.parent.iframeElementTrace(s);
        window.parent.sendUrl();
    }

    var s = pageName + "_Answer: " + ans;
    window.parent.iframeElementTrace(s);
    messageDiv.innerHTML = message;
}
