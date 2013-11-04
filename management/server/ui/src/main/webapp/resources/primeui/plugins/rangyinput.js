(function(c){var l="undefined";
var d,g,q,f,b;
var n,i,m,p;
function j(s,v){var u=typeof s[v];
return u==="function"||(!!(u=="object"&&s[v]))||u=="unknown"
}function k(s,t){return typeof(s[t])!=l
}function e(s,t){return !!(typeof(s[t])=="object"&&s[t])
}function h(s){if(window.console&&window.console.log){window.console.log("TextInputs module for Rangy not supported in your browser. Reason: "+s)
}}function o(t,u,s){if(u<0){u+=t.value.length
}if(typeof s==l){s=u
}if(s<0){s+=t.value.length
}return{start:u,end:s}
}function a(t,u,s){return{start:u,end:s,length:s-u,text:t.value.slice(u,s)}
}function r(){return e(document,"body")?document.body:document.getElementsByTagName("body")[0]
}c(document).ready(function(){var t=document.createElement("textarea");
r().appendChild(t);
if(k(t,"selectionStart")&&k(t,"selectionEnd")){d=function(w){var x=w.selectionStart,v=w.selectionEnd;
return a(w,x,v)
};
g=function(x,v,w){var y=o(x,v,w);
x.selectionStart=y.start;
x.selectionEnd=y.end
};
p=function(w,v){if(v){w.selectionEnd=w.selectionStart
}else{w.selectionStart=w.selectionEnd
}}
}else{if(j(t,"createTextRange")&&e(document,"selection")&&j(document.selection,"createRange")){d=function(z){var C=0,x=0,B,w,v,A;
var y=document.selection.createRange();
if(y&&y.parentElement()==z){v=z.value.length;
B=z.value.replace(/\r\n/g,"\n");
w=z.createTextRange();
w.moveToBookmark(y.getBookmark());
A=z.createTextRange();
A.collapse(false);
if(w.compareEndPoints("StartToEnd",A)>-1){C=x=v
}else{C=-w.moveStart("character",-v);
C+=B.slice(0,C).split("\n").length-1;
if(w.compareEndPoints("EndToEnd",A)>-1){x=v
}else{x=-w.moveEnd("character",-v);
x+=B.slice(0,x).split("\n").length-1
}}}return a(z,C,x)
};
var u=function(v,w){return w-(v.value.slice(0,w).split("\r\n").length-1)
};
g=function(z,v,y){var A=o(z,v,y);
var x=z.createTextRange();
var w=u(z,A.start);
x.collapse(true);
if(A.start==A.end){x.move("character",w)
}else{x.moveEnd("character",u(z,A.end));
x.moveStart("character",w)
}x.select()
};
p=function(x,w){var v=document.selection.createRange();
v.collapse(w);
v.select()
}
}else{r().removeChild(t);
h("No means of finding text input caret position");
return
}}r().removeChild(t);
f=function(w,z,v,x){var y;
if(z!=v){y=w.value;
w.value=y.slice(0,z)+y.slice(v)
}if(x){g(w,z,z)
}};
q=function(v){var w=d(v);
f(v,w.start,w.end,true)
};
m=function(v){var w=d(v),x;
if(w.start!=w.end){x=v.value;
v.value=x.slice(0,w.start)+x.slice(w.end)
}g(v,w.start,w.start);
return w.text
};
b=function(w,z,v,x){var y=w.value,A;
w.value=y.slice(0,v)+z+y.slice(v);
if(x){A=v+z.length;
g(w,A,A)
}};
n=function(v,y){var w=d(v),x=v.value;
v.value=x.slice(0,w.start)+y+x.slice(w.end);
var z=w.start+y.length;
g(v,z,z)
};
i=function(v,y,B){var x=d(v),A=v.value;
v.value=A.slice(0,x.start)+y+x.text+B+A.slice(x.end);
var z=x.start+y.length;
var w=z+x.length;
g(v,z,w)
};
function s(v,w){return function(){var z=this.jquery?this[0]:this;
var A=z.nodeName.toLowerCase();
if(z.nodeType==1&&(A=="textarea"||(A=="input"&&z.type=="text"))){var y=[z].concat(Array.prototype.slice.call(arguments));
var x=v.apply(this,y);
if(!w){return x
}}if(w){return this
}}
}c.fn.extend({getSelection:s(d,false),setSelection:s(g,true),collapseSelection:s(p,true),deleteSelectedText:s(q,true),deleteText:s(f,true),extractSelectedText:s(m,false),insertText:s(b,true),replaceSelectedText:s(n,true),surroundSelectedText:s(i,true)})
})
})(jQuery);