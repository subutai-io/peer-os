$(function(){$.widget("primeui.puilistbox",{options:{scrollHeight:200},_create:function(){this.element.wrap('<div class="pui-listbox pui-inputtext ui-widget ui-widget-content ui-corner-all"><div class="ui-helper-hidden-accessible"></div></div>');
this.container=this.element.parent().parent();
this.listContainer=$('<ul class="pui-listbox-list"></ul>').appendTo(this.container);
this.options.multiple=this.element.prop("multiple");
if(this.options.data){for(var b=0;
b<this.options.data.length;
b++){var a=this.options.data[b];
if(a.label){this.element.append('<option value="'+a.value+'">'+a.label+"</option>")
}else{this.element.append('<option value="'+a+'">'+a+"</option>")
}}}this.choices=this.element.children("option");
for(var b=0;
b<this.choices.length;
b++){var a=this.choices.eq(b),c=this.options.content?this.options.content.call(this,this.options.data[b]):a.text();
this.listContainer.append('<li class="pui-listbox-item ui-corner-all">'+c+"</li>")
}this.items=this.listContainer.find(".pui-listbox-item:not(.ui-state-disabled)");
if(this.container.height()>this.options.scrollHeight){this.container.height(this.options.scrollHeight)
}this._bindEvents()
},_bindEvents:function(){var a=this;
this.items.on("mouseover.puilistbox",function(){var b=$(this);
if(!b.hasClass("ui-state-highlight")){b.addClass("ui-state-hover")
}}).on("mouseout.puilistbox",function(){$(this).removeClass("ui-state-hover")
}).on("dblclick.puilistbox",function(b){a.element.trigger("dblclick");
PUI.clearSelection();
b.preventDefault()
}).on("click.puilistbox",function(b){if(a.options.multiple){a._clickMultiple(b,$(this))
}else{a._clickSingle(b,$(this))
}});
this.element.on("focus.puilistbox",function(){a.container.addClass("ui-state-focus")
}).on("blur.puilistbox",function(){a.container.removeClass("ui-state-focus")
})
},_clickSingle:function(b,a){var c=this.items.filter(".ui-state-highlight");
if(a.index()!==c.index()){if(c.length){this.unselectItem(c)
}this.selectItem(a);
this.element.trigger("change")
}this.element.trigger("click");
PUI.clearSelection();
b.preventDefault()
},_clickMultiple:function(a,j){var c=this.items.filter(".ui-state-highlight"),f=(a.metaKey||a.ctrlKey),b=(!f&&c.length===1&&c.index()===j.index());
if(!a.shiftKey){if(!f){this.unselectAll()
}if(f&&j.hasClass("ui-state-highlight")){this.unselectItem(j)
}else{this.selectItem(j);
this.cursorItem=j
}}else{if(this.cursorItem){this.unselectAll();
var g=j.index(),k=this.cursorItem.index(),h=(g>k)?k:g,e=(g>k)?(g+1):(k+1);
for(var d=h;
d<e;
d++){this.selectItem(this.items.eq(d))
}}else{this.selectItem(j);
this.cursorItem=j
}}if(!b){this.element.trigger("change")
}this.element.trigger("click");
PUI.clearSelection();
a.preventDefault()
},unselectAll:function(){this.items.removeClass("ui-state-highlight ui-state-hover");
this.choices.filter(":selected").prop("selected",false)
},selectItem:function(b){var a=null;
if($.type(b)==="number"){a=this.items.eq(b)
}else{a=b
}a.addClass("ui-state-highlight").removeClass("ui-state-hover");
this.choices.eq(a.index()).prop("selected",true);
this._trigger("itemSelect",null,this.choices.eq(a.index()))
},unselectItem:function(b){var a=null;
if($.type(b)==="number"){a=this.items.eq(b)
}else{a=b
}a.removeClass("ui-state-highlight");
this.choices.eq(a.index()).prop("selected",false);
this._trigger("itemUnselect",null,this.choices.eq(a.index()))
}})
});