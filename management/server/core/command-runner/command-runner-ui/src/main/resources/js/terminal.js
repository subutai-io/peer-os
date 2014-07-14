function termOpen() {
    if ((!term) || (term.closed)) {
        term = new Terminal(
            {
                termDiv: 'terminal',
                bgColor: 'black',
                textColor: 'white',
                handler: termHandler,
                ps: '#',
                closeOnESC: false,
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

var term;
termOpen();