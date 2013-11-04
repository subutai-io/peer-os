$(function(){$.widget("primeui.puidatatable",{options:{columns:null,datasource:null,paginator:null,selectionMode:null,rowSelect:null,rowUnselect:null,caption:null,sortField:null,sortOrder:null},_create:function(){this.id=this.element.attr("id");
if(!this.id){this.id=this.element.uniqueId().attr("id")
}this.element.addClass("pui-datatable ui-widget");
this.tableWrapper=$('<div class="pui-datatable-tablewrapper" />').appendTo(this.element);
this.table=$("<table><thead></thead><tbody></tbody></table>").appendTo(this.tableWrapper);
this.thead=this.table.children("thead");
this.tbody=this.table.children("tbody").addClass("pui-datatable-data");
if(this.options.datasource){if($.isArray(this.options.datasource)){this.data=this.options.datasource;
this._initialize()
}else{if($.type(this.options.datasource)==="function"){if(this.options.lazy){this.options.datasource.call(this,this._onDataInit,{first:0,sortField:this.options.sortField,sortorder:this.options.sortOrder})
}else{this.options.datasource.call(this,this._onDataInit)
}}}}},_initialize:function(){var a=this;
if(this.options.columns){$.each(this.options.columns,function(c,b){var d=$('<th class="ui-state-default"></th>').data("field",b.field).appendTo(a.thead);
if(b.headerText){d.text(b.headerText)
}if(b.sortable){d.addClass("pui-sortable-column").data("order",0).append('<span class="pui-sortable-column-icon ui-icon ui-icon-carat-2-n-s"></span>')
}})
}if(this.options.caption){this.table.prepend('<caption class="pui-datatable-caption ui-widget-header">'+this.options.caption+"</caption>")
}if(this.options.paginator){this.options.paginator.paginate=function(b,c){a.paginate()
};
this.options.paginator.totalRecords=this.options.paginator.totalRecords||this.data.length;
this.paginator=$("<div></div>").insertAfter(this.tableWrapper).puipaginator(this.options.paginator)
}if(this._isSortingEnabled()){this._initSorting()
}if(this.options.selectionMode){this._initSelection()
}this._renderData()
},_onDataInit:function(a){this.data=a;
if(!this.data){this.data=[]
}this._initialize()
},_onDataUpdate:function(a){this.data=a;
if(!this.data){this.data=[]
}this._renderData()
},_onLazyLoad:function(a){this.data=a;
if(!this.data){this.data=[]
}this._renderData()
},_initSorting:function(){var b=this,a=this.thead.children("th.pui-sortable-column");
a.on("mouseover.puidatatable",function(){var c=$(this);
if(!c.hasClass("ui-state-active")){c.addClass("ui-state-hover")
}}).on("mouseout.puidatatable",function(){var c=$(this);
if(!c.hasClass("ui-state-active")){c.removeClass("ui-state-hover")
}}).on("click.puidatatable",function(){var f=$(this),d=f.data("field"),c=f.data("order"),e=(c===0)?1:(c*-1),g=f.children(".pui-sortable-column-icon");
f.siblings().filter(".ui-state-active").data("order",0).removeClass("ui-state-active").children("span.pui-sortable-column-icon").removeClass("ui-icon-triangle-1-n ui-icon-triangle-1-s");
b.options.sortField=d;
b.options.sortOrder=e;
b.sort(d,e);
f.data("order",e).removeClass("ui-state-hover").addClass("ui-state-active");
if(e===-1){g.removeClass("ui-icon-triangle-1-n").addClass("ui-icon-triangle-1-s")
}else{if(e===1){g.removeClass("ui-icon-triangle-1-s").addClass("ui-icon-triangle-1-n")
}}})
},paginate:function(){if(this.options.lazy){if(this.options.selectionMode){this.selection=[]
}this.options.datasource.call(this,this._onLazyLoad,this._createStateMeta())
}else{this._renderData()
}},sort:function(b,a){if(this.options.selectionMode){this.selection=[]
}if(this.options.lazy){this.options.datasource.call(this,this._onLazyLoad,this._createStateMeta())
}else{this.data.sort(function(d,g){var f=d[b],e=g[b],c=(f<e)?-1:(f>e)?1:0;
return(a*c)
});
if(this.options.selectionMode){this.selection=[]
}if(this.paginator){this.paginator.puipaginator("option","page",0)
}this._renderData()
}},sortByField:function(d,c){var f=d.name.toLowerCase();
var e=c.name.toLowerCase();
return((f<e)?-1:((f>e)?1:0))
},_renderData:function(){if(this.data){this.tbody.html("");
var e=this.options.lazy?0:this._getFirst(),k=this._getRows();
for(var d=e;
d<(e+k);
d++){var a=this.data[d];
if(a){var h=$('<tr class="ui-widget-content" />').appendTo(this.tbody),g=(d%2===0)?"pui-datatable-even":"pui-datatable-odd";
h.addClass(g);
if(this.options.selectionMode&&PUI.inArray(this.selection,d)){h.addClass("ui-state-highlight")
}for(var c=0;
c<this.options.columns.length;
c++){var b=$("<td />").appendTo(h);
if(this.options.columns[c].content){var f=this.options.columns[c].content.call(this,a);
if($.type(f)==="string"){b.html(f)
}else{b.append(f)
}}else{b.text(a[this.options.columns[c].field])
}}}}}},_getFirst:function(){if(this.paginator){var b=this.paginator.puipaginator("option","page"),a=this.paginator.puipaginator("option","rows");
return(b*a)
}else{return 0
}},_getRows:function(){return this.paginator?this.paginator.puipaginator("option","rows"):this.data.length
},_isSortingEnabled:function(){var b=this.options.columns;
if(b){for(var a=0;
a<b.length;
a++){if(b[a].sortable){return true
}}}return false
},_initSelection:function(){var a=this;
this.selection=[];
this.rowSelector="#"+this.id+" tbody.pui-datatable-data > tr.ui-widget-content:not(.ui-datatable-empty-message)";
if(this._isMultipleSelection()){this.originRowIndex=0;
this.cursorIndex=null
}$(document).off("mouseover.puidatatable mouseout.puidatatable click.puidatatable",this.rowSelector).on("mouseover.datatable",this.rowSelector,null,function(){var b=$(this);
if(!b.hasClass("ui-state-highlight")){b.addClass("ui-state-hover")
}}).on("mouseout.datatable",this.rowSelector,null,function(){var b=$(this);
if(!b.hasClass("ui-state-highlight")){b.removeClass("ui-state-hover")
}}).on("click.datatable",this.rowSelector,null,function(b){a._onRowClick(b,this)
})
},_onRowClick:function(d,c){if(!$(d.target).is(":input,:button,a")){var f=$(c),b=f.hasClass("ui-state-highlight"),e=d.metaKey||d.ctrlKey,a=d.shiftKey;
if(b&&e){this.unselectRow(f)
}else{if(this._isSingleSelection()||(this._isMultipleSelection()&&!e&&!a)){this.unselectAllRows()
}this.selectRow(f,false,d)
}PUI.clearSelection()
}},_isSingleSelection:function(){return this.options.selectionMode==="single"
},_isMultipleSelection:function(){return this.options.selectionMode==="multiple"
},unselectAllRows:function(){this.tbody.children("tr.ui-state-highlight").removeClass("ui-state-highlight").attr("aria-selected",false);
this.selection=[]
},unselectRow:function(b,a){var c=this._getRowIndex(b);
b.removeClass("ui-state-highlight").attr("aria-selected",false);
this._removeSelection(c);
if(!a){this._trigger("rowUnselect",null,this.data[c])
}},selectRow:function(c,a,b){var d=this._getRowIndex(c);
c.removeClass("ui-state-hover").addClass("ui-state-highlight").attr("aria-selected",true);
this._addSelection(d);
if(!a){this._trigger("rowSelect",b,this.data[d])
}},getSelection:function(){var b=[];
for(var a=0;
a<this.selection.length;
a++){b.push(this.data[this.selection[a]])
}return b
},_removeSelection:function(a){this.selection=$.grep(this.selection,function(b){return b!==a
})
},_addSelection:function(a){if(!this._isSelected(a)){this.selection.push(a)
}},_isSelected:function(a){return PUI.inArray(this.selection,a)
},_getRowIndex:function(b){var a=b.index();
return this.options.paginator?this._getFirst()+a:a
},_createStateMeta:function(){var a={first:this._getFirst(),rows:this._getRows(),sortField:this.options.sortField,sortOrder:this.options.sortOrder};
return a
},_updateDatasource:function(a){this.options.datasource=a;
this.reset();
if($.isArray(this.options.datasource)){this.data=this.options.datasource;
this._renderData()
}else{if($.type(this.options.datasource)==="function"){if(this.options.lazy){this.options.datasource.call(this,this._onDataUpdate,{first:0,sortField:this.options.sortField,sortorder:this.options.sortOrder})
}else{this.options.datasource.call(this,this._onDataUpdate)
}}}},_setOption:function(a,b){if(a==="datasource"){this._updateDatasource(b)
}else{$.Widget.prototype._setOption.apply(this,arguments)
}},reset:function(){if(this.options.selectionMode){this.selection=[]
}if(this.paginator){this.paginator.puipaginator("setPage",0,true)
}this.thead.children("th.pui-sortable-column").data("order",0).filter(".ui-state-active").removeClass("ui-state-active").children("span.pui-sortable-column-icon").removeClass("ui-icon-triangle-1-n ui-icon-triangle-1-s")
}})
});