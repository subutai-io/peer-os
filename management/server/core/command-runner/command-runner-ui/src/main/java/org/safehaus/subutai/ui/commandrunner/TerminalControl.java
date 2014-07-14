package org.safehaus.subutai.ui.commandrunner;

import com.vaadin.data.Property;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.safehaus.subutai.shared.protocol.FileUtil;

/**
 * Created by daralbaev on 7/9/14.
 */
public class TerminalControl extends CssLayout {
	private TextField textField;
	private String inputPrompt;
	private String username, currentPath, machineName;

	public TerminalControl() {
		username = (String) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("username");
		currentPath = "/";
		machineName = "";
		setId("terminal");

		textField = new TextField();
		textField.setImmediate(true);
		textField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				System.out.println(textField.getValue());
			}
		});
		textField.addStyleName("terminal_submit");

		initCommandPrompt();
		addComponent(textField);

		this.setSizeFull();
	}

	public void initCommandPrompt() {
		JavaScript.getCurrent().execute(FileUtil.getContent("js/jquery-1.7.1.min.js", this));
		JavaScript.getCurrent().execute(FileUtil.getContent("js/jqconsole.min.js", this));
//		JavaScript.getCurrent().execute(FileUtil.getContent("js/terminal.js", this));
		JavaScript.getCurrent().addFunction("callback",
				new JavaScriptFunction() {
					@Override
					public void call(JSONArray arguments) throws JSONException {
						try {
//							String message = arguments.getString(0);
							Notification.show(arguments.toString());
						} catch (JSONException e) {
							Notification.show("Error: " + e.getMessage());
						}
					}
				});

		setInputPrompt();
	}

	public void setInputPrompt() {
		inputPrompt = String.format("%s@%s:%s#", username, machineName, currentPath);
		JavaScript.getCurrent().execute(FileUtil.getContent("js/terminal.js", this).replace("$prompt", inputPrompt));
	}
}
