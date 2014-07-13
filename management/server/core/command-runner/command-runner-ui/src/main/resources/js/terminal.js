$('#terminal').terminal(function (command, term) {
    $('.terminal_submit').val(command);
}, { prompt: '$cmd', name: 'test' });