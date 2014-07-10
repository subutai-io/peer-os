package org.safehaus.subutai.ui.commandrunner.custom;

import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.TextArea;

/**
 * Created by daralbaev on 7/10/14.
 */
public class TerminalArea extends AbstractExtension {

	public static void extend(TextArea textArea) {
		new TerminalArea().extend((AbstractClientConnector) textArea);
	}
}
