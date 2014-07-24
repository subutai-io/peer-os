$(function () {
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
                $('.jqconsole .jqconsole-output').remove();
                $('.jqconsole .jqconsole-old-prompt').remove();
                startPrompt();
            } else {
                // Output input with the class jqconsole-output.
                jqconsole.Write('', 'jqconsole-output');
                callback(input);
                timer = setInterval(timerPromt, 500);

                var timeoutSec = parseInt($('#timeout_txt').val()) * 1000;
                timeout = setTimeout(function () {
                    $('#terminal_indicator').parent().hide();
                    startPrompt();
                }, timeoutSec);
            }
        });
    };
    startPrompt();
});