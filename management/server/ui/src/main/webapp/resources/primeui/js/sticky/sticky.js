$(function(){$.widget("primeui.puisticky",{_create:function(){var a=this.element;
this.initialState={top:a.offset().top,width:a.width(),height:a.height()};
var c=$(window),b=this;
c.on("scroll",function(){if(c.scrollTop()>b.initialState.top){b._fix()
}else{b._restore()
}})
},_refresh:function(){$(window).off("scroll");
this._create()
},_fix:function(){if(!this.fixed){this.element.css({position:"fixed",top:0,"z-index":10000,width:this.initialState.width}).addClass("pui-shadow ui-sticky");
$('<div class="ui-sticky-ghost"></div>').height(this.initialState.height).insertBefore(this.element);
this.fixed=true
}},_restore:function(){if(this.fixed){this.element.css({position:"static",top:"auto",width:this.initialState.width}).removeClass("pui-shadow ui-sticky");
this.element.prev(".ui-sticky-ghost").remove();
this.fixed=false
}}})
});