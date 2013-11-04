$(function(){$.widget("primeui.puibasemenu",{options:{popup:false,trigger:null,my:"left top",at:"left bottom",triggerEvent:"click"},_create:function(){if(this.options.popup){this._initPopup()
}},_initPopup:function(){var a=this;
this.element.closest(".pui-menu").addClass("pui-menu-dynamic pui-shadow").appendTo(document.body);
this.positionConfig={my:this.options.my,at:this.options.at,of:this.options.trigger};
this.options.trigger.on(this.options.triggerEvent+".pui-menu",function(c){var b=$(this);
if(a.element.is(":visible")){a.hide()
}else{a.show()
}c.preventDefault()
});
$(document.body).on("click.pui-menu",function(d){var b=a.element.closest(".pui-menu");
if(b.is(":hidden")){return
}var c=$(d.target);
if(c.is(a.options.trigger.get(0))||a.options.trigger.has(c).length>0){return
}var f=b.offset();
if(d.pageX<f.left||d.pageX>f.left+b.width()||d.pageY<f.top||d.pageY>f.top+b.height()){a.hide(d)
}});
$(window).on("resize.pui-menu",function(){if(a.element.closest(".pui-menu").is(":visible")){a.align()
}})
},show:function(){this.align();
this.element.closest(".pui-menu").css("z-index",++PUI.zindex).show()
},hide:function(){this.element.closest(".pui-menu").fadeOut("fast")
},align:function(){this.element.closest(".pui-menu").css({left:"",top:""}).position(this.positionConfig)
}})
});
$(function(){$.widget("primeui.puimenu",$.primeui.puibasemenu,{options:{},_create:function(){this.element.addClass("pui-menu-list ui-helper-reset").wrap('<div class="pui-menu ui-widget ui-widget-content ui-corner-all ui-helper-clearfix" />');
this.element.children("li").each(function(){var c=$(this);
if(c.children("h3").length>0){c.addClass("ui-widget-header ui-corner-all")
}else{c.addClass("pui-menuitem ui-widget ui-corner-all");
var a=c.children("a"),b=a.data("icon");
a.addClass("pui-menuitem-link ui-corner-all").contents().wrap('<span class="ui-menuitem-text" />');
if(b){a.prepend('<span class="pui-menuitem-icon ui-icon '+b+'"></span>')
}}});
this.menuitemLinks=this.element.find(".pui-menuitem-link:not(.ui-state-disabled)");
this._bindEvents();
this._super()
},_bindEvents:function(){var a=this;
this.menuitemLinks.on("mouseenter.pui-menu",function(b){$(this).addClass("ui-state-hover")
}).on("mouseleave.pui-menu",function(b){$(this).removeClass("ui-state-hover")
});
if(this.options.popup){this.menuitemLinks.on("click.pui-menu",function(){a.hide()
})
}}})
});
$(function(){$.widget("primeui.puibreadcrumb",{_create:function(){this.element.wrap('<div class="pui-breadcrumb ui-module ui-widget ui-widget-header ui-helper-clearfix ui-corner-all" role="menu">');
this.element.children("li").each(function(b){var c=$(this);
c.attr("role","menuitem");
var a=c.children("a");
a.addClass("pui-menuitem-link ui-corner-all").contents().wrap('<span class="ui-menuitem-text" />');
if(b>0){c.before('<li class="pui-breadcrumb-chevron ui-icon ui-icon-triangle-1-e"></li>')
}else{a.addClass("ui-icon ui-icon-home")
}})
}})
});
$(function(){$.widget("primeui.puitieredmenu",$.primeui.puibasemenu,{options:{autoDisplay:true},_create:function(){this._render();
this.links=this.element.find(".pui-menuitem-link:not(.ui-state-disabled)");
this._bindEvents();
this._super()
},_render:function(){this.element.addClass("pui-menu-list ui-helper-reset").wrap('<div class="pui-tieredmenu pui-menu ui-widget ui-widget-content ui-corner-all ui-helper-clearfix" />');
this.element.parent().uniqueId();
this.options.id=this.element.parent().attr("id");
this.element.find("li").each(function(){var c=$(this),a=c.children("a"),b=a.data("icon");
a.addClass("pui-menuitem-link ui-corner-all").contents().wrap('<span class="ui-menuitem-text" />');
if(b){a.prepend('<span class="pui-menuitem-icon ui-icon '+b+'"></span>')
}c.addClass("pui-menuitem ui-widget ui-corner-all");
if(c.children("ul").length>0){c.addClass("pui-menu-parent");
c.children("ul").addClass("ui-widget-content pui-menu-list ui-corner-all ui-helper-clearfix pui-menu-child pui-shadow");
a.prepend('<span class="ui-icon ui-icon-triangle-1-e"></span>')
}})
},_bindEvents:function(){this._bindItemEvents();
this._bindDocumentHandler()
},_bindItemEvents:function(){var a=this;
this.links.on("mouseenter.pui-menu",function(){var b=$(this),d=b.parent(),c=a.options.autoDisplay;
var e=d.siblings(".pui-menuitem-active");
if(e.length===1){a._deactivate(e)
}if(c||a.active){if(d.hasClass("pui-menuitem-active")){a._reactivate(d)
}else{a._activate(d)
}}else{a._highlight(d)
}});
if(this.options.autoDisplay===false){this.rootLinks=this.element.find("> .pui-menuitem > .pui-menuitem-link");
this.rootLinks.data("primeui-tieredmenu-rootlink",this.options.id).find("*").data("primeui-tieredmenu-rootlink",this.options.id);
this.rootLinks.on("click.pui-menu",function(f){var c=$(this),d=c.parent(),b=d.children("ul.pui-menu-child");
if(b.length===1){if(b.is(":visible")){a.active=false;
a._deactivate(d)
}else{a.active=true;
a._highlight(d);
a._showSubmenu(d,b)
}}})
}this.element.parent().find("ul.pui-menu-list").on("mouseleave.pui-menu",function(b){if(a.activeitem){a._deactivate(a.activeitem)
}b.stopPropagation()
})
},_bindDocumentHandler:function(){var a=this;
$(document.body).on("click.pui-menu",function(c){var b=$(c.target);
if(b.data("primeui-tieredmenu-rootlink")===a.options.id){return
}a.active=false;
a.element.find("li.pui-menuitem-active").each(function(){a._deactivate($(this),true)
})
})
},_deactivate:function(b,a){this.activeitem=null;
b.children("a.pui-menuitem-link").removeClass("ui-state-hover");
b.removeClass("pui-menuitem-active");
if(a){b.children("ul.pui-menu-child:visible").fadeOut("fast")
}else{b.children("ul.pui-menu-child:visible").hide()
}},_activate:function(b){this._highlight(b);
var a=b.children("ul.pui-menu-child");
if(a.length===1){this._showSubmenu(b,a)
}},_reactivate:function(d){this.activeitem=d;
var c=d.children("ul.pui-menu-child"),b=c.children("li.pui-menuitem-active:first"),a=this;
if(b.length===1){a._deactivate(b)
}},_highlight:function(a){this.activeitem=a;
a.children("a.pui-menuitem-link").addClass("ui-state-hover");
a.addClass("pui-menuitem-active")
},_showSubmenu:function(b,a){a.css({left:b.outerWidth(),top:0,"z-index":++PUI.zindex});
a.show()
}})
});
$(function(){$.widget("primeui.puimenubar",$.primeui.puitieredmenu,{options:{autoDisplay:true},_create:function(){this._super();
this.element.parent().removeClass("pui-tieredmenu").addClass("pui-menubar")
},_showSubmenu:function(e,c){var d=$(window),b=null,a={"z-index":++PUI.zindex};
if(e.parent().hasClass("pui-menu-child")){a.left=e.outerWidth();
a.top=0;
b=e.offset().top-d.scrollTop()
}else{a.left=0;
a.top=e.outerHeight();
e.offset().top-d.scrollTop();
b=e.offset().top+a.top-d.scrollTop()
}c.css("height","auto");
if((b+c.outerHeight())>d.height()){a.overflow="auto";
a.height=d.height()-(b+20)
}c.css(a).show()
}})
});
$(function(){$.widget("primeui.puislidemenu",$.primeui.puibasemenu,{_create:function(){this._render();
this.rootList=this.element;
this.content=this.element.parent();
this.wrapper=this.content.parent();
this.container=this.wrapper.parent();
this.submenus=this.container.find("ul.pui-menu-list");
this.links=this.element.find("a.pui-menuitem-link:not(.ui-state-disabled)");
this.backward=this.wrapper.children("div.pui-slidemenu-backward");
this.stack=[];
this.jqWidth=this.container.width();
var a=this;
if(!this.element.hasClass("pui-menu-dynamic")){this._applyDimensions()
}this._super();
this._bindEvents()
},_render:function(){this.element.addClass("pui-menu-list ui-helper-reset").wrap('<div class="pui-menu pui-slidemenu ui-widget ui-widget-content ui-corner-all ui-helper-clearfix"/>').wrap('<div class="pui-slidemenu-wrapper" />').after('<div class="pui-slidemenu-backward ui-widget-header ui-corner-all ui-helper-clearfix">\n                    <span class="ui-icon ui-icon-triangle-1-w"></span>Back</div>').wrap('<div class="pui-slidemenu-content" />');
this.element.parent().uniqueId();
this.options.id=this.element.parent().attr("id");
this.element.find("li").each(function(){var c=$(this),a=c.children("a"),b=a.data("icon");
a.addClass("pui-menuitem-link ui-corner-all").contents().wrap('<span class="ui-menuitem-text" />');
if(b){a.prepend('<span class="pui-menuitem-icon ui-icon '+b+'"></span>')
}c.addClass("pui-menuitem ui-widget ui-corner-all");
if(c.children("ul").length>0){c.addClass("pui-menu-parent");
c.children("ul").addClass("ui-widget-content pui-menu-list ui-corner-all ui-helper-clearfix pui-menu-child ui-shadow");
a.prepend('<span class="ui-icon ui-icon-triangle-1-e"></span>')
}})
},_bindEvents:function(){var a=this;
this.links.on("mouseenter.pui-menu",function(){$(this).addClass("ui-state-hover")
}).on("mouseleave.pui-menu",function(){$(this).removeClass("ui-state-hover")
}).on("click.pui-menu",function(){var c=$(this),b=c.next();
if(b.length==1){a._forward(b)
}});
this.backward.on("click.pui-menu",function(){a._back()
})
},_forward:function(b){var c=this;
this._push(b);
var a=-1*(this._depth()*this.jqWidth);
b.show().css({left:this.jqWidth});
this.rootList.animate({left:a},500,"easeInOutCirc",function(){if(c.backward.is(":hidden")){c.backward.fadeIn("fast")
}})
},_back:function(){var c=this,b=this._pop(),d=this._depth();
var a=-1*(d*this.jqWidth);
this.rootList.animate({left:a},500,"easeInOutCirc",function(){b.hide();
if(d==0){c.backward.fadeOut("fast")
}})
},_push:function(a){this.stack.push(a)
},_pop:function(){return this.stack.pop()
},_last:function(){return this.stack[this.stack.length-1]
},_depth:function(){return this.stack.length
},_applyDimensions:function(){this.submenus.width(this.container.width());
this.wrapper.height(this.rootList.outerHeight(true)+this.backward.outerHeight(true));
this.content.height(this.rootList.outerHeight(true));
this.rendered=true
},show:function(){this.align();
this.container.css("z-index",++PUI.zindex).show();
if(!this.rendered){this._applyDimensions()
}}})
});
$(function(){$.widget("primeui.puicontextmenu",$.primeui.puitieredmenu,{options:{autoDisplay:true,target:null,event:"contextmenu"},_create:function(){this._super();
this.element.parent().removeClass("pui-tieredmenu").addClass("pui-contextmenu pui-menu-dynamic pui-shadow");
var a=this;
this.options.target=this.options.target||$(document);
if(!this.element.parent().parent().is(document.body)){this.element.parent().appendTo("body")
}this.options.target.on(this.options.event+".pui-contextmenu",function(b){a.show(b)
})
},_bindItemEvents:function(){this._super();
var a=this;
this.links.bind("click",function(){a._hide()
})
},_bindDocumentHandler:function(){var a=this;
$(document.body).bind("click.pui-contextmenu",function(b){if(a.element.parent().is(":hidden")){return
}a._hide()
})
},show:function(g){$(document.body).children(".pui-contextmenu:visible").hide();
var f=$(window),d=g.pageX,c=g.pageY,b=this.element.parent().outerWidth(),a=this.element.parent().outerHeight();
if((d+b)>(f.width())+f.scrollLeft()){d=d-b
}if((c+a)>(f.height()+f.scrollTop())){c=c-a
}if(this.options.beforeShow){this.options.beforeShow.call(this)
}this.element.parent().css({left:d,top:c,"z-index":++PUI.zindex}).show();
g.preventDefault();
g.stopPropagation()
},_hide:function(){var a=this;
this.element.parent().find("li.pui-menuitem-active").each(function(){a._deactivate($(this),true)
});
this.element.parent().fadeOut("fast")
},isVisible:function(){return this.element.parent().is(":visible")
},getTarget:function(){return this.jqTarget
}})
});