var script = document.createElement('script');
script.type = 'text/javascript';
var code = '${code}';

script.appendChild(document.createTextNode(code));
document.body.appendChild(script);