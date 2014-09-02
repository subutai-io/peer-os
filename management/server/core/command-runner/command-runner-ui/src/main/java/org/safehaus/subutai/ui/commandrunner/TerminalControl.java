package org.safehaus.subutai.ui.commandrunner;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.TextArea;
import org.json.JSONArray;
import org.json.JSONException;
import org.safehaus.subutai.common.util.FileUtil;

/**
 * Created by daralbaev on 7/9/14.
 */
public class TerminalControl extends CssLayout {
	private TerminalForm parent;
	private TextArea hiddenOutput;
	private String inputPrompt;
	private String username, currentPath, machineName;

	public TerminalControl(TerminalForm parent) {
		username = (String) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("username");
		currentPath = "/";
		machineName = "";
		setId("terminal");

		this.parent = parent;
		initCommandPrompt();

		hiddenOutput = new TextArea();
		hiddenOutput.setId("terminal_submit");
		hiddenOutput.addStyleName("terminal_submit");
		addComponent(hiddenOutput);

		this.setSizeFull();
	}

	public void initCommandPrompt() {
		JavaScript.getCurrent().execute(FileUtil.getContent("js/jquery-1.7.1.min.js", this));
		JavaScript.getCurrent().execute(FileUtil.getContent("js/jqconsole.min.js", this));

		// Adding callback to communicate javascript with Vaadin
		JavaScript.getCurrent().addFunction("callback",
				new JavaScriptFunction() {
					@Override
					public void call(JSONArray arguments) throws JSONException {
						if (arguments != null && arguments.length() > 0) {
							parent.sendCommand(arguments.getString(0));
						}
					}
				});

		setInputPrompt();
	}

	private void setInputPrompt() {
		inputPrompt = String.format("%s@%s:%s# ", username, machineName, currentPath);
		JavaScript.getCurrent().execute(FileUtil.getContent("js/terminal.js", this).replace("$prompt", inputPrompt));
	}

	public void setOutputPrompt(String output) {
		String jquery = "var d = document.createElement('span');\n" +
				"$(d).html('%s');\n" +
				"$('.jqconsole-output').last().html(d);";


		JavaScript.getCurrent().execute(String.format(jquery, SafeHtmlUtils.htmlEscape(output.replaceAll("\\n", "\\\\n"))));
	}
}
