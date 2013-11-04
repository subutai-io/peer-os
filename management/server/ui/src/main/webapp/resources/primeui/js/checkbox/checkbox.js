$(function(){$.widget("primeui.puicheckbox",{_create:function(){this.element.wrap('<div class="pui-chkbox ui-widget"><div class="ui-helper-hidden-accessible"></div></div>');
this.container=this.element.parent().parent();
this.box=$('<div class="pui-chkbox-box ui-widget ui-corner-all ui-state-default">').appendTo(this.container);
this.icon=$('<span class="pui-chkbox-icon pui-c"></span>').appendTo(this.box);
this.disabled=this.element.prop("disabled");
this.label=$('label[for="'+this.element.attr("id")+'"]');
if(this.element.prop("checked")){this.box.addClass("ui-state-active");
this.icon.addClass("ui-icon ui-icon-check")
}if(this.disabled){this.box.addClass("ui-state-disabled")
}else{this._bindEvents()
}},_bindEvents:function(){var a=this;
this.box.on("mouseover.puicheckbox",function(){if(!a.isChecked()){a.box.addClass("ui-state-hover")
}}).on("mouseout.puicheckbox",function(){a.box.removeClass("ui-state-hover")
}).on("click.puicheckbox",function(){a.toggle()
});
this.element.focus(function(){if(a.isChecked()){a.box.removeClass("ui-state-active")
}a.box.addClass("ui-state-focus")
}).blur(function(){if(a.isChecked()){a.box.addClass("ui-state-active")
}a.box.removeClass("ui-state-focus")
}).keydown(function(c){var b=$.ui.keyCode;
if(c.which==b.SPACE){c.preventDefault()
}}).keyup(function(c){var b=$.ui.keyCode;
if(c.which==b.SPACE){a.toggle(true);
c.preventDefault()
}});
this.label.on("click.puicheckbox",function(b){a.toggle();
b.preventDefault()
})
},toggle:function(a){if(this.isChecked()){this.uncheck(a)
}else{this.check(a)
}this._trigger("change",null,this.isChecked())
},isChecked:function(){return this.element.prop("checked")
},check:function(b,a){if(!this.isChecked()){this.element.prop("checked",true);
this.icon.addClass("ui-icon ui-icon-check");
if(!b){this.box.addClass("ui-state-active")
}if(!a){this.element.trigger("change")
}}},uncheck:function(){if(this.isChecked()){this.element.prop("checked",false);
this.box.removeClass("ui-state-active");
this.icon.removeClass("ui-icon ui-icon-check");
this.element.trigger("change")
}}})
});