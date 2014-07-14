$('#terminal').terminal(function (command, term) {
    $('.terminal_submit').val(command);
}, { prompt: '$cmd', greetings: "Subutai Console", width: '100%', outputLimit: 0 });