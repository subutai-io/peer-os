var jqconsole = $('#terminal').jqconsole('Subutai Terminal\n', '$prompt');
var startPrompt = function () {
    // Start the prompt with history enabled.
    jqconsole.Prompt(true, function (input) {
        // Output input with the class jqconsole-output.
        jqconsole.Write('\n', 'jqconsole-output');
        callback(input);
        // Restart the prompt.
        startPrompt();
    });
};
startPrompt();