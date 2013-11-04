$(function(){$.widget("primeui.puipanel",{options:{toggleable:false,toggleDuration:"normal",toggleOrientation:"vertical",collapsed:false,closable:false,closeDuration:"normal"},_create:function(){this.element.addClass("pui-panel ui-widget ui-widget-content ui-corner-all").contents().wrapAll('<div class="pui-panel-content ui-widget-content" />');
var c=this.element.attr("title");
if(c){this.element.prepend('<div class="pui-panel-titlebar ui-widget-header ui-helper-clearfix ui-corner-all"><span class="ui-panel-title">'+c+"</span></div>").removeAttr("title")
}this.header=this.element.children("div.pui-panel-titlebar");
this.title=this.header.children("span.ui-panel-title");
this.content=this.element.children("div.pui-panel-content");
var b=this;
if(this.options.closable){this.closer=$('<a class="pui-panel-titlebar-icon ui-corner-all ui-state-default" href="#"><span class="ui-icon ui-icon-closethick"></span></a>').appendTo(this.header).on("click.puipanel",function(d){b.close();
d.preventDefault()
})
}if(this.options.toggleable){var a=this.options.collapsed?"ui-icon-plusthick":"ui-icon-minusthick";
this.toggler=$('<a class="pui-panel-titlebar-icon ui-corner-all ui-state-default" href="#"><span class="ui-icon '+a+'"></span></a>').appendTo(this.header).on("click.puipanel",function(d){b.toggle();
d.preventDefault()
});
if(this.options.collapsed){this.content.hide()
}}this._bindEvents()
},_bindEvents:function(){this.header.find("a.pui-panel-titlebar-icon").on("hover.puipanel",function(){$(this).toggleClass("ui-state-hover")
})
},close:function(){var a=this;
this._trigger("beforeClose",null);
this.element.fadeOut(this.options.closeDuration,function(){a._trigger("afterClose",null)
})
},toggle:function(){if(this.options.collapsed){this.expand()
}else{this.collapse()
}},expand:function(){this.toggler.children("span.ui-icon").removeClass("ui-icon-plusthick").addClass("ui-icon-minusthick");
if(this.options.toggleOrientation==="vertical"){this._slideDown()
}else{if(this.options.toggleOrientation==="horizontal"){this._slideRight()
}}},collapse:function(){this.toggler.children("span.ui-icon").removeClass("ui-icon-minusthick").addClass("ui-icon-plusthick");
if(this.options.toggleOrientation==="vertical"){this._slideUp()
}else{if(this.options.toggleOrientation==="horizontal"){this._slideLeft()
}}},_slideUp:function(){var a=this;
this._trigger("beforeCollapse");
this.content.slideUp(this.options.toggleDuration,"easeInOutCirc",function(){a._trigger("afterCollapse");
a.options.collapsed=!a.options.collapsed
})
},_slideDown:function(){var a=this;
this._trigger("beforeExpand");
this.content.slideDown(this.options.toggleDuration,"easeInOutCirc",function(){a._trigger("afterExpand");
a.options.collapsed=!a.options.collapsed
})
},_slideLeft:function(){var a=this;
this.originalWidth=this.element.width();
this.title.hide();
this.toggler.hide();
this.content.hide();
this.element.animate({width:"42px"},this.options.toggleSpeed,"easeInOutCirc",function(){a.toggler.show();
a.element.addClass("pui-panel-collapsed-h");
a.options.collapsed=!a.options.collapsed
})
},_slideRight:function(){var b=this,a=this.originalWidth||"100%";
this.toggler.hide();
this.element.animate({width:a},this.options.toggleSpeed,"easeInOutCirc",function(){b.element.removeClass("pui-panel-collapsed-h");
b.title.show();
b.toggler.show();
b.options.collapsed=!b.options.collapsed;
b.content.css({visibility:"visible",display:"block",height:"auto"})
})
}})
});