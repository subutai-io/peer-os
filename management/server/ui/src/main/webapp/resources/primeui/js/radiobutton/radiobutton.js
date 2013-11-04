$(function(){var a={};
$.widget("primeui.puiradiobutton",{_create:function(){this.element.wrap('<div class="pui-radiobutton ui-widget"><div class="ui-helper-hidden-accessible"></div></div>');
this.container=this.element.parent().parent();
this.box=$('<div class="pui-radiobutton-box ui-widget pui-radiobutton-relative ui-state-default">').appendTo(this.container);
this.icon=$('<span class="pui-radiobutton-icon pui-c"></span>').appendTo(this.box);
this.disabled=this.element.prop("disabled");
this.label=$('label[for="'+this.element.attr("id")+'"]');
if(this.element.prop("checked")){this.box.addClass("ui-state-active");
this.icon.addClass("ui-icon ui-icon-bullet");
a[this.element.attr("name")]=this.box
}if(this.disabled){this.box.addClass("ui-state-disabled")
}else{this._bindEvents()
}},_bindEvents:function(){var b=this;
this.box.on("mouseover.puiradiobutton",function(){if(!b._isChecked()){b.box.addClass("ui-state-hover")
}}).on("mouseout.puiradiobutton",function(){if(!b._isChecked()){b.box.removeClass("ui-state-hover")
}}).on("click.puiradiobutton",function(){if(!b._isChecked()){b.element.trigger("click");
if($.browser.msie&&parseInt($.browser.version)<9){b.element.trigger("change")
}}});
if(this.label.length>0){this.label.on("click.puiradiobutton",function(c){b.element.trigger("click");
c.preventDefault()
})
}this.element.focus(function(){if(b._isChecked()){b.box.removeClass("ui-state-active")
}b.box.addClass("ui-state-focus")
}).blur(function(){if(b._isChecked()){b.box.addClass("ui-state-active")
}b.box.removeClass("ui-state-focus")
}).change(function(d){var c=b.element.attr("name");
if(a[c]){a[c].removeClass("ui-state-active ui-state-focus ui-state-hover").children(".pui-radiobutton-icon").removeClass("ui-icon ui-icon-bullet")
}b.icon.addClass("ui-icon ui-icon-bullet");
if(!b.element.is(":focus")){b.box.addClass("ui-state-active")
}a[c]=b.box;
b._trigger("change",null)
})
},_isChecked:function(){return this.element.prop("checked")
}})
});