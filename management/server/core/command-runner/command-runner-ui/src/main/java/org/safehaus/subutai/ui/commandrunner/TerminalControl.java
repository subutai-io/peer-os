package org.safehaus.subutai.ui.commandrunner;

import com.google.common.base.Strings;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.TextArea;

/**
 * Created by daralbaev on 7/9/14.
 */
public class TerminalControl extends TextArea {

	private String username;

	public TerminalControl() {
		username = (String) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("username");

		this.setSizeFull();
		this.setImmediate(true);
		this.setWordwrap(true);
		this.addStyleName("terminal");
		if (!Strings.isNullOrEmpty(username)) {
			this.setValue(String.format("%s#", username));
		} else {
			this.setValue("#");
		}
		this.setCursorPosition(this.getValue().length());
		this.focus();
	}
}
