$(function(){$.widget("primeui.puigrowl",{options:{sticky:false,life:3000},_create:function(){var a=this.element;
a.addClass("pui-growl ui-widget").appendTo(document.body)
},show:function(a){var b=this;
this.clear();
$.each(a,function(c,d){b._renderMessage(d)
})
},clear:function(){this.element.children("div.pui-growl-item-container").remove()
},_renderMessage:function(c){var a='<div class="pui-growl-item-container ui-state-highlight ui-corner-all ui-helper-hidden" aria-live="polite">';
a+='<div class="pui-growl-item pui-shadow">';
a+='<div class="pui-growl-icon-close ui-icon ui-icon-closethick" style="display:none"></div>';
a+='<span class="pui-growl-image pui-growl-image-'+c.severity+'" />';
a+='<div class="pui-growl-message">';
a+='<span class="pui-growl-title">'+c.summary+"</span>";
a+="<p>"+(c.detail||"")+"</p>";
a+='</div><div style="clear: both;"></div></div></div>';
var b=$(a);
this._bindMessageEvents(b);
b.appendTo(this.element).fadeIn()
},_removeMessage:function(a){a.fadeTo("normal",0,function(){a.slideUp("normal","easeInOutCirc",function(){a.remove()
})
})
},_bindMessageEvents:function(a){var c=this,b=this.options.sticky;
a.on("mouseover.puigrowl",function(){var d=$(this);
if(!d.is(":animated")){d.find("div.pui-growl-icon-close:first").show()
}}).on("mouseout.puigrowl",function(){$(this).find("div.pui-growl-icon-close:first").hide()
});
a.find("div.pui-growl-icon-close").on("click.puigrowl",function(){c._removeMessage(a);
if(!b){clearTimeout(a.data("timeout"))
}});
if(!b){this._setRemovalTimeout(a)
}},_setRemovalTimeout:function(a){var c=this;
var b=setTimeout(function(){c._removeMessage(a)
},this.options.life);
a.data("timeout",b)
}})
});