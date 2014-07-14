$(function () {
    var jqconsole = $('#terminal').jqconsole('Subutai Terminal\n', '$prompt');
    var startPrompt = function () {
        // Start the prompt with history enabled.
        jqconsole.Prompt(true, function (input) {
            // Output input with the class jqconsole-output.
            $('#pwd').val(input);
            jqconsole.Write(input + '\n', 'jqconsole-output');
            // Restart the prompt.
            startPrompt();
        });
    };
    startPrompt();
});