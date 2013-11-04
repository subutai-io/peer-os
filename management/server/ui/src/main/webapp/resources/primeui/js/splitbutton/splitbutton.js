$(function(){$.widget("primeui.puisplitbutton",{options:{icon:null,iconPos:"left",items:null},_create:function(){this.element.wrap('<div class="pui-splitbutton pui-buttonset ui-widget"></div>');
this.container=this.element.parent().uniqueId();
this.menuButton=this.container.append('<button class="pui-splitbutton-menubutton" type="button"></button>').children(".pui-splitbutton-menubutton");
this.options.disabled=this.element.prop("disabled");
if(this.options.disabled){this.menuButton.prop("disabled",true)
}this.element.puibutton(this.options).removeClass("ui-corner-all").addClass("ui-corner-left");
this.menuButton.puibutton({icon:"ui-icon-triangle-1-s"}).removeClass("ui-corner-all").addClass("ui-corner-right");
if(this.options.items&&this.options.items.length){this._renderPanel()
}this._bindEvents()
},_renderPanel:function(){this.menu=$('<div class="pui-menu pui-menu-dynamic ui-widget ui-widget-content ui-corner-all ui-helper-clearfix pui-shadow"></div>').append('<ul class="pui-menu-list ui-helper-reset"></ul>');
this.menuList=this.menu.children(".pui-menu-list");
for(var a=0;
a<this.options.items.length;
a++){var c=this.options.items[a],d=$('<li class="pui-menuitem ui-widget ui-corner-all" role="menuitem"></li>'),b=$('<a class="pui-menuitem-link ui-corner-all"><span class="pui-menuitem-icon ui-icon '+c.icon+'"></span><span class="ui-menuitem-text">'+c.text+"</span></a>");
if(c.url){b.attr("href",c.url)
}if(c.click){b.on("click.puisplitbutton",c.click)
}d.append(b).appendTo(this.menuList)
}this.menu.appendTo(this.options.appendTo||this.container);
this.options.position={my:"left top",at:"left bottom",of:this.element}
},_bindEvents:function(){var b=this;
this.menuButton.on("click.puisplitbutton",function(){if(b.menu.is(":hidden")){b.show()
}else{b.hide()
}});
this.menuList.children().on("mouseover.puisplitbutton",function(c){$(this).addClass("ui-state-hover")
}).on("mouseout.puisplitbutton",function(c){$(this).removeClass("ui-state-hover")
}).on("click.puisplitbutton",function(){b.hide()
});
$(document.body).bind("mousedown."+this.container.attr("id"),function(d){if(b.menu.is(":hidden")){return
}var c=$(d.target);
if(c.is(b.element)||b.element.has(c).length>0){return
}var f=b.menu.offset();
if(d.pageX<f.left||d.pageX>f.left+b.menu.width()||d.pageY<f.top||d.pageY>f.top+b.menu.height()){b.element.removeClass("ui-state-focus ui-state-hover");
b.hide()
}});
var a="resize."+this.container.attr("id");
$(window).unbind(a).bind(a,function(){if(b.menu.is(":visible")){b._alignPanel()
}})
},show:function(){this._alignPanel();
this.menuButton.trigger("focus");
this.menu.show();
this._trigger("show",null)
},hide:function(){this.menuButton.removeClass("ui-state-focus");
this.menu.fadeOut("fast");
this._trigger("hide",null)
},_alignPanel:function(){this.menu.css({left:"",top:"","z-index":++PUI.zindex}).position(this.options.position)
}})
});