$(function(){$.widget("primeui.puirating",{options:{stars:5,cancel:true},_create:function(){var b=this.element;
b.wrap("<div />");
this.container=b.parent();
this.container.addClass("pui-rating");
var d=b.val(),e=d==""?null:parseInt(d);
if(this.options.cancel){this.container.append('<div class="pui-rating-cancel"><a></a></div>')
}for(var c=0;
c<this.options.stars;
c++){var a=(e>c)?"pui-rating-star pui-rating-star-on":"pui-rating-star";
this.container.append('<div class="'+a+'"><a></a></div>')
}this.stars=this.container.children(".pui-rating-star");
if(b.prop("disabled")){this.container.addClass("ui-state-disabled")
}else{if(!b.prop("readonly")){this._bindEvents()
}}},_bindEvents:function(){var a=this;
this.stars.click(function(){var b=a.stars.index(this)+1;
a.setValue(b)
});
this.container.children(".pui-rating-cancel").hover(function(){$(this).toggleClass("pui-rating-cancel-hover")
}).click(function(){a.cancel()
})
},cancel:function(){this.element.val("");
this.stars.filter(".pui-rating-star-on").removeClass("pui-rating-star-on");
this._trigger("cancel",null)
},getValue:function(){var a=this.element.val();
return a==""?null:parseInt(a)
},setValue:function(b){this.element.val(b);
this.stars.removeClass("pui-rating-star-on");
for(var a=0;
a<b;
a++){this.stars.eq(a).addClass("pui-rating-star-on")
}this._trigger("rate",null,b)
}})
});