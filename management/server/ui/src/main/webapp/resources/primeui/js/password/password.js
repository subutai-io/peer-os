$(function(){$.widget("primeui.puipassword",{options:{promptLabel:"Please enter a password",weakLabel:"Weak",goodLabel:"Medium",strongLabel:"Strong",inline:false},_create:function(){this.element.puiinputtext().addClass("pui-password");
if(!this.element.prop(":disabled")){var a='<div class="pui-password-panel ui-widget ui-state-highlight ui-corner-all ui-helper-hidden">';
a+='<div class="pui-password-meter" style="background-position:0pt 0pt">&nbsp;</div>';
a+='<div class="pui-password-info">'+this.options.promptLabel+"</div>";
a+="</div>";
this.panel=$(a).insertAfter(this.element);
this.meter=this.panel.children("div.pui-password-meter");
this.infoText=this.panel.children("div.pui-password-info");
if(this.options.inline){this.panel.addClass("pui-password-panel-inline")
}else{this.panel.addClass("pui-password-panel-overlay").appendTo("body")
}this._bindEvents()
}},_destroy:function(){this.panel.remove()
},_bindEvents:function(){var b=this;
this.element.on("focus.puipassword",function(){b.show()
}).on("blur.puipassword",function(){b.hide()
}).on("keyup.puipassword",function(){var e=b.element.val(),c=null,d=null;
if(e.length==0){c=b.options.promptLabel;
d="0px 0px"
}else{var f=b._testStrength(b.element.val());
if(f<30){c=b.options.weakLabel;
d="0px -10px"
}else{if(f>=30&&f<80){c=b.options.goodLabel;
d="0px -20px"
}else{if(f>=80){c=b.options.strongLabel;
d="0px -30px"
}}}}b.meter.css("background-position",d);
b.infoText.text(c)
});
if(!this.options.inline){var a="resize."+this.element.attr("id");
$(window).unbind(a).bind(a,function(){if(b.panel.is(":visible")){b.align()
}})
}},_testStrength:function(d){var b=0,c=0,a=this;
c=d.match("[0-9]");
b+=a._normalize(c?c.length:1/4,1)*25;
c=d.match("[a-zA-Z]");
b+=a._normalize(c?c.length:1/2,3)*10;
c=d.match("[!@#$%^&*?_~.,;=]");
b+=a._normalize(c?c.length:1/6,1)*35;
c=d.match("[A-Z]");
b+=a._normalize(c?c.length:1/6,1)*30;
b*=d.length/8;
return b>100?100:b
},_normalize:function(a,c){var b=a-c;
if(b<=0){return a/c
}else{return 1+0.5*(a/(a+c/4))
}},align:function(){this.panel.css({left:"",top:"","z-index":++PUI.zindex}).position({my:"left top",at:"right top",of:this.element})
},show:function(){if(!this.options.inline){this.align();
this.panel.fadeIn()
}else{this.panel.slideDown()
}},hide:function(){if(this.options.inline){this.panel.slideUp()
}else{this.panel.fadeOut()
}}})
});