$(function(){$.widget("primeui.puidropdown",{options:{effect:"fade",effectSpeed:"normal",filter:false,filterMatchMode:"startsWith",caseSensitiveFilter:false,filterFunction:null,source:null,content:null,scrollHeight:200},_create:function(){if(this.options.source){for(var c=0;
c<this.options.source.length;
c++){var a=this.options.source[c];
if(a.label){this.element.append('<option value="'+a.value+'">'+a.label+"</option>")
}else{this.element.append('<option value="'+a+'">'+a+"</option>")
}}}this.element.wrap('<div class="pui-dropdown ui-widget ui-state-default ui-corner-all ui-helper-clearfix" />').wrap('<div class="ui-helper-hidden-accessible" />');
this.container=this.element.closest(".pui-dropdown");
this.focusElementContainer=$('<div class="ui-helper-hidden-accessible"><input type="text" /></div>').appendTo(this.container);
this.focusElement=this.focusElementContainer.children("input");
this.label=this.options.editable?$('<input type="text" class="pui-dropdown-label pui-inputtext ui-corner-all"">'):$('<label class="pui-dropdown-label pui-inputtext ui-corner-all"/>');
this.label.appendTo(this.container);
this.menuIcon=$('<div class="pui-dropdown-trigger ui-state-default ui-corner-right"><span class="ui-icon ui-icon-triangle-1-s"></span></div>').appendTo(this.container);
this.panel=$('<div class="pui-dropdown-panel ui-widget-content ui-corner-all ui-helper-hidden pui-shadow" />').appendTo(document.body);
this.itemsWrapper=$('<div class="pui-dropdown-items-wrapper" />').appendTo(this.panel);
this.itemsContainer=$('<ul class="pui-dropdown-items pui-dropdown-list ui-widget-content ui-widget ui-corner-all ui-helper-reset"></ul>').appendTo(this.itemsWrapper);
this.disabled=this.element.prop("disabled");
this.choices=this.element.children("option");
this.optGroupsSize=this.itemsContainer.children("li.puiselectonemenu-item-group").length;
if(this.options.filter){this.filterContainer=$('<div class="pui-dropdown-filter-container" />').prependTo(this.panel);
this.filterInput=$('<input type="text" autocomplete="off" class="pui-dropdown-filter pui-inputtext ui-widget ui-state-default ui-corner-all" />').appendTo(this.filterContainer);
this.filterContainer.append('<span class="ui-icon ui-icon-search"></span>')
}this._generateItems();
var e=this,d=this.choices.filter(":selected");
this.choices.filter(":disabled").each(function(){e.items.eq($(this).index()).addClass("ui-state-disabled")
});
this.triggers=this.options.editable?this.menuIcon:this.container.children(".pui-dropdown-trigger, .pui-dropdown-label");
if(this.options.editable){var b=this.label.val();
if(b===d.text()){this._highlightItem(this.items.eq(d.index()))
}else{this.items.eq(0).addClass("ui-state-highlight");
this.customInput=true;
this.customInputVal=b
}}else{this._highlightItem(this.items.eq(d.index()))
}if(!this.disabled){this._bindEvents();
this._bindConstantEvents()
}this._initDimensions()
},_generateItems:function(){for(var a=0;
a<this.choices.length;
a++){var b=this.choices.eq(a),d=b.text(),c=this.options.content?this.options.content.call(this,this.options.source[a]):d;
this.itemsContainer.append('<li data-label="'+d+'" class="pui-dropdown-item pui-dropdown-list-item ui-corner-all">'+c+"</li>")
}this.items=this.itemsContainer.children(".pui-dropdown-item")
},_bindEvents:function(){var a=this;
this.items.filter(":not(.ui-state-disabled)").each(function(b,c){a._bindItemEvents($(c))
});
this.triggers.on("mouseenter.puidropdown",function(){if(!a.container.hasClass("ui-state-focus")){a.container.addClass("ui-state-hover");
a.menuIcon.addClass("ui-state-hover")
}}).on("mouseleave.puidropdown",function(){a.container.removeClass("ui-state-hover");
a.menuIcon.removeClass("ui-state-hover")
}).on("click.puidropdown",function(b){if(a.panel.is(":hidden")){a._show()
}else{a._hide();
a._revert()
}a.container.removeClass("ui-state-hover");
a.menuIcon.removeClass("ui-state-hover");
a.focusElement.trigger("focus.puidropdown");
b.preventDefault()
});
this.focusElement.on("focus.puidropdown",function(){a.container.addClass("ui-state-focus");
a.menuIcon.addClass("ui-state-focus")
}).on("blur.puidropdown",function(){a.container.removeClass("ui-state-focus");
a.menuIcon.removeClass("ui-state-focus")
});
if(this.options.editable){this.label.on("change.pui-dropdown",function(){a._triggerChange(true);
a.customInput=true;
a.customInputVal=$(this).val();
a.items.filter(".ui-state-highlight").removeClass("ui-state-highlight");
a.items.eq(0).addClass("ui-state-highlight")
})
}this._bindKeyEvents();
if(this.options.filter){this._setupFilterMatcher();
this.filterInput.puiinputtext();
this.filterInput.on("keyup.pui-dropdown",function(){a._filter($(this).val())
})
}},_bindItemEvents:function(a){var b=this;
a.on("mouseover.puidropdown",function(){var c=$(this);
if(!c.hasClass("ui-state-highlight")){$(this).addClass("ui-state-hover")
}}).on("mouseout.puidropdown",function(){$(this).removeClass("ui-state-hover")
}).on("click.puidropdown",function(){b._selectItem($(this))
})
},_bindConstantEvents:function(){var a=this;
$(document.body).bind("mousedown.pui-dropdown",function(b){if(a.panel.is(":hidden")){return
}var c=a.panel.offset();
if(b.target===a.label.get(0)||b.target===a.menuIcon.get(0)||b.target===a.menuIcon.children().get(0)){return
}if(b.pageX<c.left||b.pageX>c.left+a.panel.width()||b.pageY<c.top||b.pageY>c.top+a.panel.height()){a._hide();
a._revert()
}});
this.resizeNS="resize."+this.id;
this._unbindResize();
this._bindResize()
},_bindKeyEvents:function(){var a=this;
this.focusElement.on("keydown.puiselectonemenu",function(h){var l=$.ui.keyCode,j=h.which;
switch(j){case l.UP:case l.LEFT:var d=a._getActiveItem(),b=d.prevAll(":not(.ui-state-disabled,.ui-selectonemenu-item-group):first");
if(b.length==1){if(a.panel.is(":hidden")){a._selectItem(b)
}else{a._highlightItem(b);
PUI.scrollInView(a.itemsWrapper,b)
}}h.preventDefault();
break;
case l.DOWN:case l.RIGHT:var d=a._getActiveItem(),f=d.nextAll(":not(.ui-state-disabled,.ui-selectonemenu-item-group):first");
if(f.length==1){if(a.panel.is(":hidden")){if(h.altKey){a._show()
}else{a._selectItem(f)
}}else{a._highlightItem(f);
PUI.scrollInView(a.itemsWrapper,f)
}}h.preventDefault();
break;
case l.ENTER:case l.NUMPAD_ENTER:if(a.panel.is(":hidden")){a._show()
}else{a._selectItem(a._getActiveItem())
}h.preventDefault();
break;
case l.TAB:if(a.panel.is(":visible")){a._revert();
a._hide()
}break;
case l.ESCAPE:if(a.panel.is(":visible")){a._revert();
a._hide()
}break;
default:var c=String.fromCharCode((96<=j&&j<=105)?j-48:j),i=a.items.filter(".ui-state-highlight");
var g=a._search(c,i.index()+1,a.options.length);
if(!g){g=a._search(c,0,i.index())
}if(g){if(a.panel.is(":hidden")){a._selectItem(g)
}else{a._highlightItem(g);
PUI.scrollInView(a.itemsWrapper,g)
}}break
}})
},_initDimensions:function(){var b=this.element.attr("style");
if(!b||b.indexOf("width")==-1){this.container.width(this.element.outerWidth(true)+5)
}this.label.width(this.container.width()-this.menuIcon.width());
var a=this.container.innerWidth();
if(this.panel.outerWidth()<a){this.panel.width(a)
}this.element.parent().addClass("ui-helper-hidden").removeClass("ui-helper-hidden-accessible");
if(this.options.scrollHeight&&this.panel.outerHeight()>this.options.scrollHeight){this.itemsWrapper.height(this.options.scrollHeight)
}},_selectItem:function(f,b){var e=this.choices.eq(this._resolveItemIndex(f)),d=this.choices.filter(":selected"),a=e.val()==d.val(),c=null;
if(this.options.editable){c=(!a)||(e.text()!=this.label.val())
}else{c=!a
}if(c){this._highlightItem(f);
this.element.val(e.val());
this._triggerChange();
if(this.options.editable){this.customInput=false
}}if(!b){this.focusElement.trigger("focus.puidropdown")
}if(this.panel.is(":visible")){this._hide()
}},_highlightItem:function(a){this.items.filter(".ui-state-highlight").removeClass("ui-state-highlight");
a.addClass("ui-state-highlight");
this._setLabel(a.data("label"))
},_triggerChange:function(a){this.changed=false;
if(this.options.change){this._trigger("change")
}if(!a){this.value=this.choices.filter(":selected").val()
}},_resolveItemIndex:function(a){if(this.optGroupsSize===0){return a.index()
}else{return a.index()-a.prevAll("li.pui-dropdown-item-group").length
}},_setLabel:function(a){if(this.options.editable){this.label.val(a)
}else{if(a==="&nbsp;"){this.label.html("&nbsp;")
}else{this.label.text(a)
}}},_bindResize:function(){var a=this;
$(window).bind(this.resizeNS,function(b){if(a.panel.is(":visible")){a._alignPanel()
}})
},_unbindResize:function(){$(window).unbind(this.resizeNS)
},_unbindEvents:function(){this.items.off();
this.triggers.off();
this.input.off();
this.focusInput.off();
this.label.off()
},_alignPanel:function(){this.panel.css({left:"",top:""}).position({my:"left top",at:"left bottom",of:this.container})
},_show:function(){this._alignPanel();
this.panel.css("z-index",++PUI.zindex);
if(this.options.effect!="none"){this.panel.show(this.options.effect,{},this.options.effectSpeed)
}else{this.panel.show()
}this.preShowValue=this.choices.filter(":selected")
},_hide:function(){this.panel.hide()
},_revert:function(){if(this.options.editable&&this.customInput){this._setLabel(this.customInputVal);
this.items.filter(".ui-state-active").removeClass("ui-state-active");
this.items.eq(0).addClass("ui-state-active")
}else{this._highlightItem(this.items.eq(this.preShowValue.index()))
}},_getActiveItem:function(){return this.items.filter(".ui-state-highlight")
},_setupFilterMatcher:function(){this.filterMatchers={startsWith:this._startsWithFilter,contains:this._containsFilter,endsWith:this._endsWithFilter,custom:this.options.filterFunction};
this.filterMatcher=this.filterMatchers[this.options.filterMatchMode]
},_startsWithFilter:function(b,a){return b.indexOf(a)===0
},_containsFilter:function(b,a){return b.indexOf(a)!==-1
},_endsWithFilter:function(b,a){return b.indexOf(a,b.length-a.length)!==-1
},_filter:function(e){this.initialHeight=this.initialHeight||this.itemsWrapper.height();
var f=this.options.caseSensitiveFilter?$.trim(e):$.trim(e).toLowerCase();
if(f===""){this.items.filter(":hidden").show()
}else{for(var a=0;
a<this.choices.length;
a++){var c=this.choices.eq(a),b=this.options.caseSensitiveFilter?c.text():c.text().toLowerCase(),d=this.items.eq(a);
if(this.filterMatcher(b,f)){d.show()
}else{d.hide()
}}}if(this.itemsContainer.height()<this.initialHeight){this.itemsWrapper.css("height","auto")
}else{this.itemsWrapper.height(this.initialHeight)
}},_search:function(d,e,a){for(var b=e;
b<a;
b++){var c=this.choices.eq(b);
if(c.text().indexOf(d)==0){return this.items.eq(b)
}}return null
},getSelectedValue:function(){return this.element.val()
},getSelectedLabel:function(){return this.choices.filter(":selected").text()
},selectValue:function(b){var a=this.choices.filter('[value="'+b+'"]');
this._selectItem(this.items.eq(a.index()),true)
},addOption:function(b,d){var c=$('<li data-label="'+b+'" class="pui-dropdown-item pui-dropdown-list-item ui-corner-all">'+b+"</li>"),a=$('<option value="'+d+'">'+b+"</option>");
a.appendTo(this.element);
this._bindItemEvents(c);
c.appendTo(this.itemsContainer);
this.items.add(c)
}})
});