$(function(){$.widget("primeui.puiprogressbar",{options:{value:0,labelTemplate:"{value}%",complete:null,easing:"easeInOutCirc",effectSpeed:"normal",showLabel:true},_create:function(){this.element.addClass("pui-progressbar ui-widget ui-widget-content ui-corner-all").append('<div class="pui-progressbar-value ui-widget-header ui-corner-all"></div>').append('<div class="pui-progressbar-label"></div>');
this.jqValue=this.element.children(".pui-progressbar-value");
this.jqLabel=this.element.children(".pui-progressbar-label");
if(this.options.value!==0){this._setValue(this.options.value,false)
}this.enableARIA()
},_setValue:function(d,b){var c=(b===undefined||b)?true:false;
if(d>=0&&d<=100){if(d===0){this.jqValue.hide().css("width","0%").removeClass("ui-corner-right");
this.jqLabel.hide()
}else{if(c){this.jqValue.show().animate({width:d+"%"},this.options.effectSpeed,this.options.easing)
}else{this.jqValue.show().css("width",d+"%")
}if(this.options.labelTemplate&&this.options.showLabel){var a=this.options.labelTemplate.replace(/{value}/gi,d);
this.jqLabel.html(a).show()
}if(d===100){this._trigger("complete")
}}this.options.value=d;
this.element.attr("aria-valuenow",d)
}},_getValue:function(){return this.options.value
},enableARIA:function(){this.element.attr("role","progressbar").attr("aria-valuemin",0).attr("aria-valuenow",this.options.value).attr("aria-valuemax",100)
},_setOption:function(a,b){if(a==="value"){this._setValue(b)
}$.Widget.prototype._setOption.apply(this,arguments)
},_destroy:function(){}})
});