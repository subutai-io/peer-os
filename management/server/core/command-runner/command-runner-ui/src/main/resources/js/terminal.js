var term;

function termOpen() {
    if ((!term) || (term.closed)) {
        term = new Terminal(
            {
                x: 220,
                y: 70,
                termDiv: 'terminal',
                bgColor: '#232e45',
                handler: termHandler,
                wrapping: true
            }
        );
        term.open();
    }
}

function termHandler() {
    // default handler + exit
    this.newLine();
    this.write('You wrote: ');
    this.write('You wrote: ' + this.lineBuffer.replace(/%/g, '%%'));
    this.newLine();
    this.prompt();
}