$(function(){$.widget("primeui.puiinputtext",{_create:function(){var a=this.element,b=a.prop("disabled");
a.addClass("pui-inputtext ui-widget ui-state-default ui-corner-all");
if(b){a.addClass("ui-state-disabled")
}else{a.hover(function(){a.toggleClass("ui-state-hover")
}).focus(function(){a.addClass("ui-state-focus")
}).blur(function(){a.removeClass("ui-state-focus")
})
}a.attr("role","textbox").attr("aria-disabled",b).attr("aria-readonly",a.prop("readonly")).attr("aria-multiline",a.is("textarea"))
},_destroy:function(){}})
});