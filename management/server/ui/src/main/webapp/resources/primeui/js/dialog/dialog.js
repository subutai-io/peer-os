$(function(){$.widget("primeui.puidialog",{options:{draggable:true,resizable:true,location:"center",minWidth:150,minHeight:25,height:"auto",width:"300px",visible:false,modal:false,showEffect:null,hideEffect:null,effectOptions:{},effectSpeed:"normal",closeOnEscape:true,rtl:false,closable:true,minimizable:false,maximizable:false,appendTo:null,buttons:null},_create:function(){this.element.addClass("pui-dialog ui-widget ui-widget-content ui-helper-hidden ui-corner-all pui-shadow").contents().wrapAll('<div class="pui-dialog-content ui-widget-content" />');
this.element.prepend('<div class="pui-dialog-titlebar ui-widget-header ui-helper-clearfix ui-corner-top"><span id="'+this.element.attr("id")+'_label" class="pui-dialog-title">'+this.element.attr("title")+"</span>").removeAttr("title");
if(this.options.buttons){this.footer=$('<div class="pui-dialog-buttonpane ui-widget-content ui-helper-clearfix"></div>').appendTo(this.element);
for(var b=0;
b<this.options.buttons.length;
b++){var c=this.options.buttons[b],a=$('<button type="button"></button>').appendTo(this.footer);
if(c.text){a.text(c.text)
}a.puibutton(c)
}}if(this.options.rtl){this.element.addClass("pui-dialog-rtl")
}this.content=this.element.children(".pui-dialog-content");
this.titlebar=this.element.children(".pui-dialog-titlebar");
if(this.options.closable){this._renderHeaderIcon("pui-dialog-titlebar-close","ui-icon-close")
}if(this.options.minimizable){this._renderHeaderIcon("pui-dialog-titlebar-maximize","ui-icon-extlink")
}if(this.options.minimizable){this._renderHeaderIcon("pui-dialog-titlebar-minimize","ui-icon-minus")
}this.icons=this.titlebar.children(".pui-dialog-titlebar-icon");
this.closeIcon=this.titlebar.children(".pui-dialog-titlebar-close");
this.minimizeIcon=this.titlebar.children(".pui-dialog-titlebar-minimize");
this.maximizeIcon=this.titlebar.children(".pui-dialog-titlebar-maximize");
this.blockEvents="focus.puidialog mousedown.puidialog mouseup.puidialog keydown.puidialog keyup.puidialog";
this.parent=this.element.parent();
this.element.css({width:this.options.width,height:"auto"});
this.content.height(this.options.height);
this._bindEvents();
if(this.options.draggable){this._setupDraggable()
}if(this.options.resizable){this._setupResizable()
}if(this.options.appendTo){this.element.appendTo(this.options.appendTo)
}if($(document.body).children(".pui-dialog-docking-zone").length==0){$(document.body).append('<div class="pui-dialog-docking-zone"></div>')
}this._applyARIA();
if(this.options.visible){this.show()
}},_renderHeaderIcon:function(a,b){this.titlebar.append('<a class="pui-dialog-titlebar-icon '+a+' ui-corner-all" href="#" role="button"><span class="ui-icon '+b+'"></span></a>')
},_enableModality:function(){var b=this,a=$(document);
this.modality=$('<div id="'+this.element.attr("id")+'_modal" class="ui-widget-overlay"></div>').appendTo(document.body).css({width:a.width(),height:a.height(),"z-index":this.element.css("z-index")-1});
a.bind("keydown.puidialog",function(e){if(e.keyCode==$.ui.keyCode.TAB){var d=b.content.find(":tabbable"),f=d.filter(":first"),c=d.filter(":last");
if(e.target===c[0]&&!e.shiftKey){f.focus(1);
return false
}else{if(e.target===f[0]&&e.shiftKey){c.focus(1);
return false
}}}}).bind(this.blockEvents,function(c){if($(c.target).zIndex()<b.element.zIndex()){return false
}})
},_disableModality:function(){this.modality.remove();
this.modality=null;
$(document).unbind(this.blockEvents).unbind("keydown.dialog")
},show:function(){if(this.element.is(":visible")){return
}if(!this.positionInitialized){this._initPosition()
}this._trigger("beforeShow",null);
if(this.options.showEffect){var a=this;
this.element.show(this.options.showEffect,this.options.effectOptions,this.options.effectSpeed,function(){a._postShow()
})
}else{this.element.show();
this._postShow()
}this._moveToTop();
if(this.options.modal){this._enableModality()
}},_postShow:function(){this._trigger("afterShow",null);
this.element.attr({"aria-hidden":false,"aria-live":"polite"});
this._applyFocus()
},hide:function(){if(this.element.is(":hidden")){return
}this._trigger("beforeHide",null);
if(this.options.hideEffect){var a=this;
this.element.hide(this.options.hideEffect,this.options.effectOptions,this.options.effectSpeed,function(){a._postHide()
})
}else{this.element.hide();
this._postHide()
}if(this.options.modal){this._disableModality()
}},_postHide:function(){this._trigger("afterHide",null);
this.element.attr({"aria-hidden":true,"aria-live":"off"})
},_applyFocus:function(){this.element.find(":not(:submit):not(:button):input:visible:enabled:first").focus()
},_bindEvents:function(){var a=this;
this.icons.mouseover(function(){$(this).addClass("ui-state-hover")
}).mouseout(function(){$(this).removeClass("ui-state-hover")
});
this.closeIcon.on("click.puidialog",function(b){a.hide();
b.preventDefault()
});
this.maximizeIcon.click(function(b){a.toggleMaximize();
b.preventDefault()
});
this.minimizeIcon.click(function(b){a.toggleMinimize();
b.preventDefault()
});
if(this.options.closeOnEscape){$(document).on("keydown.dialog_"+this.element.attr("id"),function(d){var c=$.ui.keyCode,b=parseInt(a.element.css("z-index"))===PUI.zindex;
if(d.which===c.ESCAPE&&a.element.is(":visible")&&b){a.hide()
}})
}if(this.options.modal){$(window).on("resize.puidialog",function(){$(document.body).children(".ui-widget-overlay").css({width:$(document).width(),height:$(document).height()})
})
}},_setupDraggable:function(){this.element.draggable({cancel:".pui-dialog-content, .pui-dialog-titlebar-close",handle:".pui-dialog-titlebar",containment:"document"})
},_setupResizable:function(){this.element.resizable({minWidth:this.options.minWidth,minHeight:this.options.minHeight,alsoResize:this.content,containment:"document"});
this.resizers=this.element.children(".ui-resizable-handle")
},_initPosition:function(){this.element.css({left:0,top:0});
if(/(center|left|top|right|bottom)/.test(this.options.location)){this.options.location=this.options.location.replace(","," ");
this.element.position({my:"center",at:this.options.location,collision:"fit",of:window,using:function(f){var d=f.left<0?0:f.left,e=f.top<0?0:f.top;
$(this).css({left:d,top:e})
}})
}else{var b=this.options.position.split(","),a=$.trim(b[0]),c=$.trim(b[1]);
this.element.offset({left:a,top:c})
}this.positionInitialized=true
},_moveToTop:function(){this.element.css("z-index",++PUI.zindex)
},toggleMaximize:function(){if(this.minimized){this.toggleMinimize()
}if(this.maximized){this.element.removeClass("pui-dialog-maximized");
this._restoreState();
this.maximizeIcon.removeClass("ui-state-hover").children(".ui-icon").removeClass("ui-icon-newwin").addClass("ui-icon-extlink");
this.maximized=false
}else{this._saveState();
var a=$(window);
this.element.addClass("pui-dialog-maximized").css({width:a.width()-6,height:a.height()}).offset({top:a.scrollTop(),left:a.scrollLeft()});
this.content.css({width:"auto",height:"auto"});
this.maximizeIcon.removeClass("ui-state-hover").children(".ui-icon").removeClass("ui-icon-extlink").addClass("ui-icon-newwin");
this.maximized=true;
this._trigger("maximize")
}},toggleMinimize:function(){var a=true,c=$(document.body).children(".pui-dialog-docking-zone");
if(this.maximized){this.toggleMaximize();
a=false
}var b=this;
if(this.minimized){this.element.appendTo(this.parent).removeClass("pui-dialog-minimized").css({position:"fixed","float":"none"});
this._restoreState();
this.content.show();
this.minimizeIcon.removeClass("ui-state-hover").children(".ui-icon").removeClass("ui-icon-plus").addClass("ui-icon-minus");
this.minimized=false;
if(this.options.resizable){this.resizers.show()
}if(this.footer){this.footer.show()
}}else{this._saveState();
if(a){this.element.effect("transfer",{to:c,className:"pui-dialog-minimizing"},500,function(){b._dock(c);
b.element.addClass("pui-dialog-minimized")
})
}else{this._dock(c)
}}},_dock:function(a){this.element.appendTo(a).css("position","static");
this.element.css({height:"auto",width:"auto","float":"left"});
this.content.hide();
this.minimizeIcon.removeClass("ui-state-hover").children(".ui-icon").removeClass("ui-icon-minus").addClass("ui-icon-plus");
this.minimized=true;
if(this.options.resizable){this.resizers.hide()
}if(this.footer){this.footer.hide()
}a.css("z-index",++PUI.zindex);
this._trigger("minimize")
},_saveState:function(){this.state={width:this.element.width(),height:this.element.height()};
var a=$(window);
this.state.offset=this.element.offset();
this.state.windowScrollLeft=a.scrollLeft();
this.state.windowScrollTop=a.scrollTop()
},_restoreState:function(){this.element.width(this.state.width).height(this.state.height);
var a=$(window);
this.element.offset({top:this.state.offset.top+(a.scrollTop()-this.state.windowScrollTop),left:this.state.offset.left+(a.scrollLeft()-this.state.windowScrollLeft)})
},_applyARIA:function(){this.element.attr({role:"dialog","aria-labelledby":this.element.attr("id")+"_title","aria-hidden":!this.options.visible});
this.titlebar.children("a.pui-dialog-titlebar-icon").attr("role","button")
}})
});