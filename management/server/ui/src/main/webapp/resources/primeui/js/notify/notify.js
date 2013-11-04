$(function(){$.widget("primeui.puinotify",{options:{position:"top",visible:false,animate:true,effectSpeed:"normal",easing:"swing"},_create:function(){this.element.addClass("pui-notify pui-notify-"+this.options.position+" ui-widget ui-widget-content pui-shadow").wrapInner('<div class="pui-notify-content" />').appendTo(document.body);
this.content=this.element.children(".pui-notify-content");
this.closeIcon=$('<span class="ui-icon ui-icon-closethick pui-notify-close"></span>').appendTo(this.element);
this._bindEvents();
if(this.options.visible){this.show()
}},_bindEvents:function(){var a=this;
this.closeIcon.on("click.puinotify",function(){a.hide()
})
},show:function(a){var b=this;
if(a){this.update(a)
}this.element.css("z-index",++PUI.zindex);
this._trigger("beforeShow");
if(this.options.animate){this.element.slideDown(this.options.effectSpeed,this.options.easing,function(){b._trigger("afterShow")
})
}else{this.element.show();
b._trigger("afterShow")
}},hide:function(){var a=this;
this._trigger("beforeHide");
if(this.options.animate){this.element.slideUp(this.options.effectSpeed,this.options.easing,function(){a._trigger("afterHide")
})
}else{this.element.hide();
a._trigger("afterHide")
}},update:function(a){this.content.html(a)
}})
});