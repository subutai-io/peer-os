$(function(){$.widget("primeui.puitree",{options:{nodes:null,lazy:false,animate:false,selectionMode:null,icons:null},_create:function(){this.element.uniqueId().addClass("pui-tree ui-widget ui-widget-content ui-corner-all").append('<ul class="pui-tree-container"></ul>');
this.rootContainer=this.element.children(".pui-tree-container");
if(this.options.selectionMode){this.selection=[]
}this._bindEvents();
if($.type(this.options.nodes)==="array"){this._renderNodes(this.options.nodes,this.rootContainer)
}else{if($.type(this.options.nodes)==="function"){this.options.nodes.call(this,{},this._initData)
}else{throw"Unsupported type. nodes option can be either an array or a function"
}}},_renderNodes:function(b,a){for(var c=0;
c<b.length;
c++){this._renderNode(b[c],a)
}},_renderNode:function(c,b){var k=this.options.lazy?c.leaf:!(c.children&&c.children.length),d=c.iconType||"def",h=c.expanded,m=this.options.selectionMode?(c.selectable===false?false:true):false,f=k?"pui-treenode-leaf-icon":(c.expanded?"pui-tree-toggler ui-icon ui-icon-triangle-1-s":"pui-tree-toggler ui-icon ui-icon-triangle-1-e"),g=k?"pui-treenode pui-treenode-leaf":"pui-treenode pui-treenode-parent",p=$('<li class="'+g+'"></li>'),o=$('<span class="pui-treenode-content"></span>');
p.data("puidata",c.data).appendTo(b);
if(m){o.addClass("pui-treenode-selectable")
}o.append('<span class="'+f+'"></span>').append('<span class="pui-treenode-icon"></span>').append('<span class="pui-treenode-label ui-corner-all">'+c.label+"</span>").appendTo(p);
var a=this.options.icons&&this.options.icons[d];
if(a){var j=o.children(".pui-treenode-icon"),l=($.type(a)==="string")?a:(h?a.expanded:a.collapsed);
j.addClass("ui-icon "+l)
}if(!k){var n=$('<ul class="pui-treenode-children"></ul>');
if(!c.expanded){n.hide()
}n.appendTo(p);
if(c.children){for(var e=0;
e<c.children.length;
e++){this._renderNode(c.children[e],n)
}}}},_initData:function(a){this._renderNodes(a,this.rootContainer)
},_handleNodeData:function(b,a){this._renderNodes(b,a.children(".pui-treenode-children"));
this._showNodeChildren(a);
a.data("puiloaded",true)
},_bindEvents:function(){var e=this,c=this.element.attr("id"),b="#"+c+" .pui-tree-toggler";
$(document).off("click.puitree-"+c,b).on("click.puitree-"+c,b,null,function(h){var f=$(this),g=f.closest("li");
if(g.hasClass("pui-treenode-expanded")){e.collapseNode(g)
}else{e.expandNode(g)
}});
if(this.options.selectionMode){var a="#"+c+" .pui-treenode-selectable .pui-treenode-label",d="#"+c+" .pui-treenode-selectable.pui-treenode-content";
$(document).off("mouseout.puitree-"+c+" mouseover.puitree-"+c,a).on("mouseout.puitree-"+c,a,null,function(){$(this).removeClass("ui-state-hover")
}).on("mouseover.puitree-"+c,a,null,function(){$(this).addClass("ui-state-hover")
}).off("click.puitree-"+c,d).on("click.puitree-"+c,d,null,function(f){e._nodeClick(f,$(this))
})
}},expandNode:function(a){this._trigger("beforeExpand",null,{node:a,data:a.data("puidata")});
if(this.options.lazy&&!a.data("puiloaded")){this.options.nodes.call(this,{node:a,data:a.data("puidata")},this._handleNodeData)
}else{this._showNodeChildren(a)
}},collapseNode:function(e){this._trigger("beforeCollapse",null,{node:e,data:e.data("puidata")});
e.removeClass("pui-treenode-expanded");
var a=e.iconType||"def",c=this.options.icons&&this.options.icons[a];
if(c&&$.type(c)!=="string"){e.find("> .pui-treenode-content > .pui-treenode-icon").removeClass(c.expanded).addClass(c.collapsed)
}var d=e.find("> .pui-treenode-content > .pui-tree-toggler"),b=e.children(".pui-treenode-children");
d.addClass("ui-icon-triangle-1-e").removeClass("ui-icon-triangle-1-s");
if(this.options.animate){b.slideUp("fast")
}else{b.hide()
}this._trigger("afterCollapse",null,{node:e,data:e.data("puidata")})
},_showNodeChildren:function(d){d.addClass("pui-treenode-expanded").attr("aria-expanded",true);
var a=d.iconType||"def",b=this.options.icons&&this.options.icons[a];
if(b&&$.type(b)!=="string"){d.find("> .pui-treenode-content > .pui-treenode-icon").removeClass(b.collapsed).addClass(b.expanded)
}var c=d.find("> .pui-treenode-content > .pui-tree-toggler");
c.addClass("ui-icon-triangle-1-s").removeClass("ui-icon-triangle-1-e");
if(this.options.animate){d.children(".pui-treenode-children").slideDown("fast")
}else{d.children(".pui-treenode-children").show()
}this._trigger("afterExpand",null,{node:d,data:d.data("puidata")})
},_nodeClick:function(d,a){PUI.clearSelection();
if($(d.target).is(":not(.pui-tree-toggler)")){var c=a.parent();
var b=this._isNodeSelected(c.data("puidata")),e=d.metaKey||d.ctrlKey;
if(b&&e){this.unselectNode(c)
}else{if(this._isSingleSelection()||(this._isMultipleSelection()&&!e)){this.unselectAllNodes()
}this.selectNode(c)
}}},selectNode:function(a){a.attr("aria-selected",true).find("> .pui-treenode-content > .pui-treenode-label").removeClass("ui-state-hover").addClass("ui-state-highlight");
this._addToSelection(a.data("puidata"));
this._trigger("nodeSelect",null,{node:a,data:a.data("puidata")})
},unselectNode:function(a){a.attr("aria-selected",false).find("> .pui-treenode-content > .pui-treenode-label").removeClass("ui-state-highlight ui-state-hover");
this._removeFromSelection(a.data("puidata"));
this._trigger("nodeUnselect",null,{node:a,data:a.data("puidata")})
},unselectAllNodes:function(){this.selection=[];
this.element.find(".pui-treenode-label.ui-state-highlight").each(function(){$(this).removeClass("ui-state-highlight").closest(".ui-treenode").attr("aria-selected",false)
})
},_addToSelection:function(b){if(b){var a=this._isNodeSelected(b);
if(!a){this.selection.push(b)
}}},_removeFromSelection:function(c){if(c){var a=-1;
for(var b=0;
b<this.selection.length;
b++){var d=this.selection[b];
if(d&&(JSON.stringify(d)===JSON.stringify(c))){a=b;
break
}}if(a>=0){this.selection.splice(a,1)
}}},_isNodeSelected:function(c){var b=false;
if(c){for(var a=0;
a<this.selection.length;
a++){var d=this.selection[a];
if(d&&(JSON.stringify(d)===JSON.stringify(c))){b=true;
break
}}}return b
},_isSingleSelection:function(){return this.options.selectionMode&&this.options.selectionMode==="single"
},_isMultipleSelection:function(){return this.options.selectionMode&&this.options.selectionMode==="multiple"
}})
});