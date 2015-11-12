jQuery(function($, undefined) {
    $('#term_demo').terminal(function(command, term) {
        if (command !== '') {
            try {
                var result = window.eval(command);
                if (result !== undefined) {
                    term.echo(new String(result));
                }
            } catch(e) {
                term.error(new String(e));
            }
        } else {
         term.echo('');
     }
 }, {
    greetings: 'Javascript Interpreter',
    name: 'js_demo',
    height: 400,
    prompt: 'js> '});
});

