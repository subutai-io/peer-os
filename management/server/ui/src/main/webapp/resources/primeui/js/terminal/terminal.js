$(function(){$.widget("primeui.puiterminal",{options:{welcomeMessage:"",prompt:"prime $",handler:null},_create:function(){this.element.addClass("pui-terminal ui-widget ui-widget-content ui-corner-all").append("<div>"+this.options.welcomeMessage+"</div>").append('<div class="pui-terminal-content"></div>').append('<div><span class="pui-terminal-prompt">'+this.options.prompt+'</span><input type="text" class="pui-terminal-input" autocomplete="off"></div>');
this.promptContainer=this.element.find("> div:last-child > span.pui-terminal-prompt");
this.content=this.element.children(".pui-terminal-content");
this.input=this.promptContainer.next();
this.commands=[];
this.commandIndex=0;
this._bindEvents()
},_bindEvents:function(){var a=this;
this.input.on("keydown.terminal",function(c){var b=$.ui.keyCode;
switch(c.which){case b.UP:if(a.commandIndex>0){a.input.val(a.commands[--a.commandIndex])
}c.preventDefault();
break;
case b.DOWN:if(a.commandIndex<(a.commands.length-1)){a.input.val(a.commands[++a.commandIndex])
}else{a.commandIndex=a.commands.length;
a.input.val("")
}c.preventDefault();
break;
case b.ENTER:case b.NUMPAD_ENTER:a._processCommand();
c.preventDefault();
break
}})
},_processCommand:function(){var a=this.input.val();
this.commands.push();
this.commandIndex++;
if(this.options.handler&&$.type(this.options.handler)==="function"){this.options.handler.call(this,a,this._updateContent)
}},_updateContent:function(a){var b=$("<div></div>");
b.append("<span>"+this.options.prompt+'</span><span class="pui-terminal-command">'+this.input.val()+"</span>").append("<div>"+a+"</div>").appendTo(this.content);
this.input.val("");
this.element.scrollTop(this.content.height())
},clear:function(){this.content.html("");
this.input.val("")
}})
});