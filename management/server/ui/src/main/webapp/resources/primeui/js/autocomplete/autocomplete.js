$(function(){$.widget("primeui.puiautocomplete",{options:{delay:300,minQueryLength:1,multiple:false,dropdown:false,scrollHeight:200,forceSelection:false,effect:null,effectOptions:{},effectSpeed:"normal",content:null,caseSensitive:false},_create:function(){this.element.puiinputtext();
this.panel=$('<div class="pui-autocomplete-panel ui-widget-content ui-corner-all ui-helper-hidden pui-shadow"></div>').appendTo("body");
if(this.options.multiple){this.element.wrap('<ul class="pui-autocomplete-multiple ui-widget pui-inputtext ui-state-default ui-corner-all"><li class="pui-autocomplete-input-token"></li></ul>');
this.inputContainer=this.element.parent();
this.multiContainer=this.inputContainer.parent()
}else{if(this.options.dropdown){this.dropdown=$('<button type="button" class="pui-button ui-widget ui-state-default ui-corner-right pui-button-icon-only"><span class="pui-button-icon-primary ui-icon ui-icon-triangle-1-s"></span><span class="pui-button-text">&nbsp;</span></button>').insertAfter(this.element);
this.element.removeClass("ui-corner-all").addClass("ui-corner-left")
}}this._bindEvents()
},_bindEvents:function(){var a=this;
this._bindKeyEvents();
if(this.options.dropdown){this.dropdown.on("hover.puiautocomplete",function(){if(!a.element.prop("disabled")){a.dropdown.toggleClass("ui-state-hover")
}}).on("mousedown.puiautocomplete",function(){if(!a.element.prop("disabled")){a.dropdown.addClass("ui-state-active")
}}).on("mouseup.puiautocomplete",function(){if(!a.element.prop("disabled")){a.dropdown.removeClass("ui-state-active");
a.search("");
a.element.focus()
}}).on("focus.puiautocomplete",function(){a.dropdown.addClass("ui-state-focus")
}).on("blur.puiautocomplete",function(){a.dropdown.removeClass("ui-state-focus")
}).on("keydown.puiautocomplete",function(c){var b=$.ui.keyCode;
if(c.which==b.ENTER||c.which==b.NUMPAD_ENTER){a.search("");
a.input.focus();
c.preventDefault()
}})
}if(this.options.multiple){this.multiContainer.on("hover.puiautocomplete",function(){$(this).toggleClass("ui-state-hover")
}).on("click.puiautocomplete",function(){a.element.trigger("focus")
});
this.element.on("focus.pui-autocomplete",function(){a.multiContainer.addClass("ui-state-focus")
}).on("blur.pui-autocomplete",function(b){a.multiContainer.removeClass("ui-state-focus")
})
}if(this.options.forceSelection){this.currentItems=[this.element.val()];
this.element.on("blur.puiautocomplete",function(){var d=$(this).val(),c=false;
for(var b=0;
b<a.currentItems.length;
b++){if(a.currentItems[b]===d){c=true;
break
}}if(!c){a.element.val("")
}})
}$(document.body).bind("mousedown.puiautocomplete",function(b){if(a.panel.is(":hidden")){return
}if(b.target===a.element.get(0)){return
}var c=a.panel.offset();
if(b.pageX<c.left||b.pageX>c.left+a.panel.width()||b.pageY<c.top||b.pageY>c.top+a.panel.height()){a.hide()
}});
$(window).bind("resize."+this.element.id,function(){if(a.panel.is(":visible")){a._alignPanel()
}})
},_bindKeyEvents:function(){var a=this;
this.element.on("keyup.puiautocomplete",function(g){var f=$.ui.keyCode,b=g.which,d=true;
if(b==f.UP||b==f.LEFT||b==f.DOWN||b==f.RIGHT||b==f.TAB||b==f.SHIFT||b==f.ENTER||b==f.NUMPAD_ENTER){d=false
}if(d){var c=a.element.val();
if(!c.length){a.hide()
}if(c.length>=a.options.minQueryLength){if(a.timeout){clearTimeout(a.timeout)
}a.timeout=setTimeout(function(){a.search(c)
},a.options.delay)
}}}).on("keydown.puiautocomplete",function(g){if(a.panel.is(":visible")){var f=$.ui.keyCode,d=a.items.filter(".ui-state-highlight");
switch(g.which){case f.UP:case f.LEFT:var c=d.prev();
if(c.length==1){d.removeClass("ui-state-highlight");
c.addClass("ui-state-highlight");
if(a.options.scrollHeight){PUI.scrollInView(a.panel,c)
}}g.preventDefault();
break;
case f.DOWN:case f.RIGHT:var b=d.next();
if(b.length==1){d.removeClass("ui-state-highlight");
b.addClass("ui-state-highlight");
if(a.options.scrollHeight){PUI.scrollInView(a.panel,b)
}}g.preventDefault();
break;
case f.ENTER:case f.NUMPAD_ENTER:d.trigger("click");
g.preventDefault();
break;
case f.ALT:case 224:break;
case f.TAB:d.trigger("click");
a.hide();
break
}}})
},_bindDynamicEvents:function(){var a=this;
this.items.on("mouseover.puiautocomplete",function(){var b=$(this);
if(!b.hasClass("ui-state-highlight")){a.items.filter(".ui-state-highlight").removeClass("ui-state-highlight");
b.addClass("ui-state-highlight")
}}).on("click.puiautocomplete",function(d){var c=$(this);
if(a.options.multiple){var b='<li class="pui-autocomplete-token ui-state-active ui-corner-all ui-helper-hidden">';
b+='<span class="pui-autocomplete-token-icon ui-icon ui-icon-close" />';
b+='<span class="pui-autocomplete-token-label">'+c.data("label")+"</span></li>";
$(b).data(c.data()).insertBefore(a.inputContainer).fadeIn().children(".pui-autocomplete-token-icon").on("click.pui-autocomplete",function(g){var f=$(this).parent();
a._removeItem(f);
a._trigger("unselect",g,f)
});
a.element.val("").trigger("focus")
}else{a.element.val(c.data("label")).focus()
}a._trigger("select",d,c);
a.hide()
})
},search:function(h){this.query=this.options.caseSensitive?h:h.toLowerCase();
var f={query:this.query};
if(this.options.completeSource){if($.isArray(this.options.completeSource)){var b=this.options.completeSource,g=[],a=($.trim(h)==="");
for(var c=0;
c<b.length;
c++){var e=b[c],d=e.label||e;
if(!this.options.caseSensitive){d=d.toLowerCase()
}if(a||d.indexOf(this.query)===0){g.push({label:b[c],value:e})
}}this._handleData(g)
}else{this.options.completeSource.call(this,f,this._handleData)
}}},_handleData:function(e){var g=this;
this.panel.html("");
this.listContainer=$('<ul class="pui-autocomplete-items pui-autocomplete-list ui-widget-content ui-widget ui-corner-all ui-helper-reset"></ul>').appendTo(this.panel);
for(var b=0;
b<e.length;
b++){var c=$('<li class="pui-autocomplete-item pui-autocomplete-list-item ui-corner-all"></li>');
c.data(e[b]);
if(this.options.content){c.html(this.options.content.call(this,e[b]))
}else{c.text(e[b].label)
}this.listContainer.append(c)
}this.items=this.listContainer.children(".pui-autocomplete-item");
this._bindDynamicEvents();
if(this.items.length>0){var f=g.items.eq(0),d=this.panel.is(":hidden");
f.addClass("ui-state-highlight");
if(g.query.length>0&&!g.options.content){g.items.each(function(){var i=$(this),k=i.html(),h=new RegExp(PUI.escapeRegExp(g.query),"gi"),j=k.replace(h,'<span class="pui-autocomplete-query">$&</span>');
i.html(j)
})
}if(this.options.forceSelection){this.currentItems=[];
$.each(e,function(h,j){g.currentItems.push(j.label)
})
}if(g.options.scrollHeight){var a=d?g.panel.height():g.panel.children().height();
if(a>g.options.scrollHeight){g.panel.height(g.options.scrollHeight)
}else{g.panel.css("height","auto")
}}if(d){g.show()
}else{g._alignPanel()
}}else{this.panel.hide()
}},show:function(){this._alignPanel();
if(this.options.effect){this.panel.show(this.options.effect,{},this.options.effectSpeed)
}else{this.panel.show()
}},hide:function(){this.panel.hide();
this.panel.css("height","auto")
},_removeItem:function(a){a.fadeOut("fast",function(){var b=$(this);
b.remove()
})
},_alignPanel:function(){var b=null;
if(this.options.multiple){b=this.multiContainer.innerWidth()-(this.element.position().left-this.multiContainer.position().left)
}else{if(this.panel.is(":visible")){b=this.panel.children(".pui-autocomplete-items").outerWidth()
}else{this.panel.css({visibility:"hidden",display:"block"});
b=this.panel.children(".pui-autocomplete-items").outerWidth();
this.panel.css({visibility:"visible",display:"none"})
}var a=this.element.outerWidth();
if(b<a){b=a
}}this.panel.css({left:"",top:"",width:b,"z-index":++PUI.zindex}).position({my:"left top",at:"left bottom",of:this.element})
}})
});