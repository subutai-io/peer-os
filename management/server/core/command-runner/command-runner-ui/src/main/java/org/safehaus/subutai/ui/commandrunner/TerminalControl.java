package org.safehaus.subutai.ui.commandrunner;

import com.vaadin.ui.TextArea;

/**
 * Created by daralbaev on 7/9/14.
 */
public class TerminalControl extends TextArea {
	public TerminalControl() {
		this.setSizeFull();
		this.setImmediate(true);
		this.setWordwrap(true);
		this.addStyleName("terminal");
		this.setValue(String.format("%s#", getSession().getAttribute("username")));
		this.setCursorPosition(this.getValue().length());
		this.focus();
	}
}
