$(function(){$.widget("primeui.puilightbox",{options:{iframeWidth:640,iframeHeight:480,iframe:false},_create:function(){this.options.mode=this.options.iframe?"iframe":(this.element.children("div").length==1)?"inline":"image";
var a='<div class="pui-lightbox ui-widget ui-helper-hidden ui-corner-all pui-shadow">';
a+='<div class="pui-lightbox-content-wrapper">';
a+='<a class="ui-state-default pui-lightbox-nav-left ui-corner-right ui-helper-hidden"><span class="ui-icon ui-icon-carat-1-w">go</span></a>';
a+='<div class="pui-lightbox-content ui-corner-all"></div>';
a+='<a class="ui-state-default pui-lightbox-nav-right ui-corner-left ui-helper-hidden"><span class="ui-icon ui-icon-carat-1-e">go</span></a>';
a+="</div>";
a+='<div class="pui-lightbox-caption ui-widget-header"><span class="pui-lightbox-caption-text"></span>';
a+='<a class="pui-lightbox-close ui-corner-all" href="#"><span class="ui-icon ui-icon-closethick"></span></a><div style="clear:both" /></div>';
a+="</div>";
this.panel=$(a).appendTo(document.body);
this.contentWrapper=this.panel.children(".pui-lightbox-content-wrapper");
this.content=this.contentWrapper.children(".pui-lightbox-content");
this.caption=this.panel.children(".pui-lightbox-caption");
this.captionText=this.caption.children(".pui-lightbox-caption-text");
this.closeIcon=this.caption.children(".pui-lightbox-close");
if(this.options.mode==="image"){this._setupImaging()
}else{if(this.options.mode==="inline"){this._setupInline()
}else{if(this.options.mode==="iframe"){this._setupIframe()
}}}this._bindCommonEvents();
this.links.data("puilightbox-trigger",true).find("*").data("puilightbox-trigger",true);
this.closeIcon.data("puilightbox-trigger",true).find("*").data("puilightbox-trigger",true)
},_bindCommonEvents:function(){var a=this;
this.closeIcon.hover(function(){$(this).toggleClass("ui-state-hover")
}).click(function(b){a.hide();
b.preventDefault()
});
$(document.body).bind("click.pui-lightbox",function(c){if(a.isHidden()){return
}var b=$(c.target);
if(b.data("puilightbox-trigger")){return
}var d=a.panel.offset();
if(c.pageX<d.left||c.pageX>d.left+a.panel.width()||c.pageY<d.top||c.pageY>d.top+a.panel.height()){a.hide()
}});
$(window).resize(function(){if(!a.isHidden()){$(document.body).children(".ui-widget-overlay").css({width:$(document).width(),height:$(document).height()})
}})
},_setupImaging:function(){var a=this;
this.links=this.element.children("a");
this.content.append('<img class="ui-helper-hidden"></img>');
this.imageDisplay=this.content.children("img");
this.navigators=this.contentWrapper.children("a");
this.imageDisplay.load(function(){var d=$(this);
a._scaleImage(d);
var c=(a.panel.width()-d.width())/2,b=(a.panel.height()-d.height())/2;
a.content.removeClass("pui-lightbox-loading").animate({width:d.width(),height:d.height()},500,function(){d.fadeIn();
a._showNavigators();
a.caption.slideDown()
});
a.panel.animate({left:"+="+c,top:"+="+b},500)
});
this.navigators.hover(function(){$(this).toggleClass("ui-state-hover")
}).click(function(c){var d=$(this);
a._hideNavigators();
if(d.hasClass("pui-lightbox-nav-left")){var b=a.current==0?a.links.length-1:a.current-1;
a.links.eq(b).trigger("click")
}else{var b=a.current==a.links.length-1?0:a.current+1;
a.links.eq(b).trigger("click")
}c.preventDefault()
});
this.links.click(function(c){var b=$(this);
if(a.isHidden()){a.content.addClass("pui-lightbox-loading").width(32).height(32);
a.show()
}else{a.imageDisplay.fadeOut(function(){$(this).css({width:"auto",height:"auto"});
a.content.addClass("pui-lightbox-loading")
});
a.caption.slideUp()
}setTimeout(function(){a.imageDisplay.attr("src",b.attr("href"));
a.current=b.index();
var d=b.attr("title");
if(d){a.captionText.html(d)
}},1000);
c.preventDefault()
})
},_scaleImage:function(g){var f=$(window),c=f.width(),b=f.height(),d=g.width(),a=g.height(),e=a/d;
if(d>=c&&e<=1){d=c*0.75;
a=d*e
}else{if(a>=b){a=b*0.75;
d=a/e
}}g.css({width:d+"px",height:a+"px"})
},_setupInline:function(){this.links=this.element.children("a");
this.inline=this.element.children("div").addClass("pui-lightbox-inline");
this.inline.appendTo(this.content).show();
var a=this;
this.links.click(function(b){a.show();
var c=$(this).attr("title");
if(c){a.captionText.html(c);
a.caption.slideDown()
}b.preventDefault()
})
},_setupIframe:function(){var a=this;
this.links=this.element;
this.iframe=$('<iframe frameborder="0" style="width:'+this.options.iframeWidth+"px;height:"+this.options.iframeHeight+'px;border:0 none; display: block;"></iframe>').appendTo(this.content);
if(this.options.iframeTitle){this.iframe.attr("title",this.options.iframeTitle)
}this.element.click(function(b){if(!a.iframeLoaded){a.content.addClass("pui-lightbox-loading").css({width:a.options.iframeWidth,height:a.options.iframeHeight});
a.show();
a.iframe.on("load",function(){a.iframeLoaded=true;
a.content.removeClass("pui-lightbox-loading")
}).attr("src",a.element.attr("href"))
}else{a.show()
}var c=a.element.attr("title");
if(c){a.caption.html(c);
a.caption.slideDown()
}b.preventDefault()
})
},show:function(){this.center();
this.panel.css("z-index",++PUI.zindex).show();
if(!this.modality){this._enableModality()
}this._trigger("show")
},hide:function(){this.panel.fadeOut();
this._disableModality();
this.caption.hide();
if(this.options.mode==="image"){this.imageDisplay.hide().attr("src","").removeAttr("style");
this._hideNavigators()
}this._trigger("hide")
},center:function(){var c=$(window),b=(c.width()/2)-(this.panel.width()/2),a=(c.height()/2)-(this.panel.height()/2);
this.panel.css({left:b,top:a})
},_enableModality:function(){this.modality=$('<div class="ui-widget-overlay"></div>').css({width:$(document).width(),height:$(document).height(),"z-index":this.panel.css("z-index")-1}).appendTo(document.body)
},_disableModality:function(){this.modality.remove();
this.modality=null
},_showNavigators:function(){this.navigators.zIndex(this.imageDisplay.zIndex()+1).show()
},_hideNavigators:function(){this.navigators.hide()
},isHidden:function(){return this.panel.is(":hidden")
},showURL:function(a){if(a.width){this.iframe.attr("width",a.width)
}if(a.height){this.iframe.attr("height",a.height)
}this.iframe.attr("src",a.src);
this.show()
}})
});