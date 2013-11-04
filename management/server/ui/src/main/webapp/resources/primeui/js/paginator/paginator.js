$(function(){ElementHandlers={"{FirstPageLink}":{markup:'<span class="pui-paginator-first pui-paginator-element ui-state-default ui-corner-all"><span class="ui-icon ui-icon-seek-first">p</span></span>',create:function(b){var a=$(this.markup);
if(b.options.page===0){a.addClass("ui-state-disabled")
}a.on("click.puipaginator",function(){if(!$(this).hasClass("ui-state-disabled")){b.option("page",0)
}});
return a
},update:function(a,b){if(b.page===0){a.addClass("ui-state-disabled").removeClass("ui-state-hover ui-state-active")
}else{a.removeClass("ui-state-disabled")
}}},"{PreviousPageLink}":{markup:'<span class="pui-paginator-prev pui-paginator-element ui-state-default ui-corner-all"><span class="ui-icon ui-icon-seek-prev">p</span></span>',create:function(b){var a=$(this.markup);
if(b.options.page===0){a.addClass("ui-state-disabled")
}a.on("click.puipaginator",function(){if(!$(this).hasClass("ui-state-disabled")){b.option("page",b.options.page-1)
}});
return a
},update:function(a,b){if(b.page===0){a.addClass("ui-state-disabled").removeClass("ui-state-hover ui-state-active")
}else{a.removeClass("ui-state-disabled")
}}},"{NextPageLink}":{markup:'<span class="pui-paginator-next pui-paginator-element ui-state-default ui-corner-all"><span class="ui-icon ui-icon-seek-next">p</span></span>',create:function(b){var a=$(this.markup);
if(b.options.page===(b.getPageCount()-1)){a.addClass("ui-state-disabled").removeClass("ui-state-hover ui-state-active")
}a.on("click.puipaginator",function(){if(!$(this).hasClass("ui-state-disabled")){b.option("page",b.options.page+1)
}});
return a
},update:function(a,b){if(b.page===(b.pageCount-1)){a.addClass("ui-state-disabled").removeClass("ui-state-hover ui-state-active")
}else{a.removeClass("ui-state-disabled")
}}},"{LastPageLink}":{markup:'<span class="pui-paginator-last pui-paginator-element ui-state-default ui-corner-all"><span class="ui-icon ui-icon-seek-end">p</span></span>',create:function(b){var a=$(this.markup);
if(b.options.page===(b.getPageCount()-1)){a.addClass("ui-state-disabled").removeClass("ui-state-hover ui-state-active")
}a.on("click.puipaginator",function(){if(!$(this).hasClass("ui-state-disabled")){b.option("page",b.getPageCount()-1)
}});
return a
},update:function(a,b){if(b.page===(b.pageCount-1)){a.addClass("ui-state-disabled").removeClass("ui-state-hover ui-state-active")
}else{a.removeClass("ui-state-disabled")
}}},"{PageLinks}":{markup:'<span class="pui-paginator-pages"></span>',create:function(h){var e=$(this.markup),b=this.calculateBoundaries({page:h.options.page,pageLinks:h.options.pageLinks,pageCount:h.getPageCount(),}),g=b[0],a=b[1];
for(var d=g;
d<=a;
d++){var f=(d+1),c=$('<span class="pui-paginator-page pui-paginator-element ui-state-default ui-corner-all">'+f+"</span>");
if(d===h.options.page){c.addClass("ui-state-active")
}c.on("click.puipaginator",function(j){var i=$(this);
if(!i.hasClass("ui-state-disabled")&&!i.hasClass("ui-state-active")){h.option("page",parseInt(i.text())-1)
}});
e.append(c)
}return e
},update:function(g,a){var j=g.children(),d=this.calculateBoundaries({page:a.page,pageLinks:a.pageLinks,pageCount:a.pageCount,}),b=d[0],e=d[1],c=0;
j.filter(".ui-state-active").removeClass("ui-state-active");
for(var h=b;
h<=e;
h++){var k=(h+1),f=j.eq(c);
if(h===a.page){f.addClass("ui-state-active")
}f.text(k);
c++
}},calculateBoundaries:function(c){var d=c.page,h=c.pageLinks,b=c.pageCount,e=Math.min(h,b);
var g=Math.max(0,parseInt(Math.ceil(d-((e)/2)))),a=Math.min(b-1,g+e-1);
var f=h-(a-g+1);
g=Math.max(0,g-f);
return[g,a]
}}};
$.widget("primeui.puipaginator",{options:{pageLinks:5,totalRecords:0,page:0,rows:0,template:"{FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink}"},_create:function(){this.element.addClass("pui-paginator ui-widget-header");
this.paginatorElements=[];
var a=this.options.template.split(/[ ]+/);
for(var b=0;
b<a.length;
b++){var e=a[b],d=ElementHandlers[e];
if(d){var c=d.create(this);
this.paginatorElements[e]=c;
this.element.append(c)
}}this._bindEvents()
},_bindEvents:function(){this.element.find("span.pui-paginator-element").on("mouseover.puipaginator",function(){var a=$(this);
if(!a.hasClass("ui-state-active")&&!a.hasClass("ui-state-disabled")){a.addClass("ui-state-hover")
}}).on("mouseout.puipaginator",function(){var a=$(this);
if(a.hasClass("ui-state-hover")){a.removeClass("ui-state-hover")
}})
},_setOption:function(a,b){if(a==="page"){this.setPage(b)
}else{$.Widget.prototype._setOption.apply(this,arguments)
}},setPage:function(d,a){var b=this.getPageCount();
if(d>=0&&d<b&&this.options.page!==d){var c={first:this.options.rows*d,rows:this.options.rows,page:d,pageCount:b,pageLinks:this.options.pageLinks};
this.options.page=d;
if(!a){this._trigger("paginate",null,c)
}this.updateUI(c)
}},updateUI:function(b){for(var a in this.paginatorElements){ElementHandlers[a].update(this.paginatorElements[a],b)
}},getPageCount:function(){return Math.ceil(this.options.totalRecords/this.options.rows)||1
}})
});