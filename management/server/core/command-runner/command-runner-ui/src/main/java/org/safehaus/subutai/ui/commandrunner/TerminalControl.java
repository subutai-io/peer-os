package org.safehaus.subutai.ui.commandrunner;

import com.vaadin.event.FieldEvents;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.TextField;
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
		textField.addStyleName("terminal_submit");

		initCommandPrompt();
		addComponent(textField);

		textField.addTextChangeListener(new FieldEvents.TextChangeListener() {
			@Override
			public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {
				System.out.println(textChangeEvent.getText());
			}
		});

		this.setSizeFull();
	}

	private void initCommandPrompt() {
		JavaScript.getCurrent().execute(FileUtil.getContent("js/jquery-1.7.1.min.js", this));
		JavaScript.getCurrent().execute(FileUtil.getContent("js/jquery.terminal-min.js", this));
		JavaScript.getCurrent().execute(FileUtil.getContent("js/jquery.mousewheel-min.js", this));

		setInputPrompt();
	}

	public void setInputPrompt() {
		inputPrompt = String.format("%s@%s:%s>", username, machineName, currentPath);
		JavaScript.getCurrent().execute(FileUtil.getContent("js/terminal.js", this).replace("$cmd", inputPrompt));
	}
}
