$(function(){var a={primaryStyles:["fontFamily","fontSize","fontWeight","fontVariant","fontStyle","paddingLeft","paddingTop","paddingBottom","paddingRight","marginLeft","marginTop","marginBottom","marginRight","borderLeftColor","borderTopColor","borderBottomColor","borderRightColor","borderLeftStyle","borderTopStyle","borderBottomStyle","borderRightStyle","borderLeftWidth","borderTopWidth","borderBottomWidth","borderRightWidth","line-height","outline"],specificStyle:{"word-wrap":"break-word","overflow-x":"hidden","overflow-y":"auto"},simulator:$('<div id="textarea_simulator"/>').css({position:"absolute",top:0,left:0,visibility:"hidden"}).appendTo(document.body),toHtml:function(b){return b.replace(/\n/g,"<br>").split(" ").join('<span style="white-space:prev-wrap">&nbsp;</span>')
},getCaretPosition:function(){var c=a,n=this,g=n[0],d=n.offset();
if($.browser.msie){g.focus();
var h=document.selection.createRange();
$("#hskeywords").val(g.scrollTop);
return{left:h.boundingLeft-d.left,top:parseInt(h.boundingTop)-d.top+g.scrollTop+document.documentElement.scrollTop+parseInt(n.getComputedStyle("fontSize"))}
}c.simulator.empty();
$.each(c.primaryStyles,function(p,q){n.cloneStyle(c.simulator,q)
});
c.simulator.css($.extend({width:n.width(),height:n.height()},c.specificStyle));
var l=n.val(),e=n.getCursorPosition();
var f=l.substring(0,e),m=l.substring(e);
var j=$('<span class="before"/>').html(c.toHtml(f)),o=$('<span class="focus"/>'),b=$('<span class="after"/>').html(c.toHtml(m));
c.simulator.append(j).append(o).append(b);
var i=o.offset(),k=c.simulator.offset();
return{top:i.top-k.top-g.scrollTop+($.browser.mozilla?0:parseInt(n.getComputedStyle("fontSize"))),left:o[0].offsetLeft-c.simulator[0].offsetLeft-g.scrollLeft}
}};
$.fn.extend({getComputedStyle:function(c){if(this.length==0){return
}var d=this[0];
var b=this.css(c);
b=b||($.browser.msie?d.currentStyle[c]:document.defaultView.getComputedStyle(d,null)[c]);
return b
},cloneStyle:function(c,b){var d=this.getComputedStyle(b);
if(!!d){$(c).css(b,d)
}},cloneAllStyle:function(e,d){var c=this[0];
for(var b in c.style){var f=c.style[b];
typeof f=="string"||typeof f=="number"?this.cloneStyle(e,b):NaN
}},getCursorPosition:function(){var e=this[0],b=0;
if("selectionStart" in e){b=e.selectionStart
}else{if("selection" in document){var c=document.selection.createRange();
if(parseInt($.browser.version)>6){e.focus();
var g=document.selection.createRange().text.length;
c.moveStart("character",-e.value.length);
b=c.text.length-g
}else{var h=document.body.createTextRange();
h.moveToElementText(e);
for(;
h.compareEndPoints("StartToStart",c)<0;
b++){h.moveStart("character",1)
}for(var d=0;
d<=b;
d++){if(e.value.charAt(d)=="\n"){b++
}}var f=e.value.split("\n").length-1;
b-=f;
return b
}}}return b
},getCaretPosition:a.getCaretPosition})
});