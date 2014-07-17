var timer;
var timeout;
var jqconsole = $('#terminal').jqconsole('Subutai Terminal\n', '$prompt');

var timerPromt = function () {
    var output = $('.jqconsole-output').last().find('span').html();
    if (output !== "") {
        clearInterval(timer);
        clearInterval(timeout);
        startPrompt();
    }
}

var startPrompt = function () {
    // Start the prompt with history enabled.
    jqconsole.Prompt(true, function (input) {
        if (input === "clear") {
            var terminal = $('.jqconsole');
            var header = $(terminal).find('span').first().html();
            var prompt = $(terminal).find('span').last().html();
            $(terminal).html(header);
            $(terminal).append(prompt);
        }
        // Output input with the class jqconsole-output.
        jqconsole.Write('', 'jqconsole-output');
        callback(input);
        timer = setInterval(timerPromt, 2000);

        var timeoutSec = parseInt($('#timeout_txt').val()) * 1000;
        timeout = setTimeout(startPrompt, timeoutSec);
    });
};
startPrompt();