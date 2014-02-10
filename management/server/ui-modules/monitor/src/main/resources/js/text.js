var s = document.createElement('script');
s.type = 'text/javascript';
var code = 'function hello(a) { console.log(a); }; hello(789); ';

s.appendChild(document.createTextNode(code));
document.body.appendChild(s);