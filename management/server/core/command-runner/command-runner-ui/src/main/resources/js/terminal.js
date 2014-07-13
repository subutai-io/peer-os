$('#terminal').terminal(function (command, term) {
    if (command == 'test') {
        term.echo("you just typed 'test'");
    } else {
        term.echo('unknown command');
    }
}, { prompt: '$cmd', name: 'test' });