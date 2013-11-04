$(function(){$.widget("primeui.puibutton",{options:{value:null,icon:null,iconPos:"left",click:null},_create:function(){var b=this.element,d=b.text(),e=this.options.value||(d===""?"pui-button":d),c=b.prop("disabled"),a=null;
if(this.options.icon){a=(e==="pui-button")?"pui-button-icon-only":"pui-button-text-icon-"+this.options.iconPos
}else{a="pui-button-text-only"
}if(c){a+=" ui-state-disabled"
}this.element.addClass("pui-button ui-widget ui-state-default ui-corner-all "+a).text("");
if(this.options.icon){this.element.append('<span class="pui-button-icon-'+this.options.iconPos+" ui-icon "+this.options.icon+'" />')
}this.element.append('<span class="pui-button-text">'+e+"</span>");
b.attr("role","button").attr("aria-disabled",c);
if(!c){this._bindEvents()
}},_bindEvents:function(){var a=this.element,b=this;
a.on("mouseover.puibutton",function(){if(!a.prop("disabled")){a.addClass("ui-state-hover")
}}).on("mouseout.puibutton",function(){$(this).removeClass("ui-state-active ui-state-hover")
}).on("mousedown.puibutton",function(){if(!a.hasClass("ui-state-disabled")){a.addClass("ui-state-active").removeClass("ui-state-hover")
}}).on("mouseup.puibutton",function(c){a.removeClass("ui-state-active").addClass("ui-state-hover");
b._trigger("click",c)
}).on("focus.puibutton",function(){a.addClass("ui-state-focus")
}).on("blur.puibutton",function(){a.removeClass("ui-state-focus")
}).on("keydown.puibutton",function(c){if(c.keyCode==$.ui.keyCode.SPACE||c.keyCode==$.ui.keyCode.ENTER||c.keyCode==$.ui.keyCode.NUMPAD_ENTER){a.addClass("ui-state-active")
}}).on("keyup.puibutton",function(){a.removeClass("ui-state-active")
});
return this
},_unbindEvents:function(){this.element.off("mouseover.puibutton mouseout.puibutton mousedown.puibutton mouseup.puibutton focus.puibutton blur.puibutton keydown.puibutton keyup.puibutton")
},disable:function(){this._unbindEvents();
this.element.attr({disabled:"disabled","aria-disabled":true}).addClass("ui-state-disabled")
},enable:function(){this._bindEvents();
this.element.removeAttr("disabled").attr("aria-disabled",false).removeClass("ui-state-disabled")
}})
});