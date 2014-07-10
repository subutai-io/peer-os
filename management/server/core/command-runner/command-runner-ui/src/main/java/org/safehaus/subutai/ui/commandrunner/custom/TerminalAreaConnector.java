package org.safehaus.subutai.ui.commandrunner.custom;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.VTextArea;
import com.vaadin.shared.ui.Connect;

/**
 * Created by daralbaev on 7/10/14.
 */

@Connect (TerminalArea.class)
public class TerminalAreaConnector extends AbstractExtensionConnector {

	private VTextArea textArea;
	private KeyPressHandler keyPressHandler = new KeyPressHandler() {
		@Override
		public void onKeyPress(KeyPressEvent event) {
			if (textArea.isReadOnly() || !textArea.isEnabled()) {
				return;
			}

			int keyCode = event.getNativeEvent().getKeyCode();
			System.out.println(keyCode + " ");
		}
	};

	@Override
	protected void extend(ServerConnector target){
		textArea = (VTextArea) ((ComponentConnector)target).getWidget();
		textArea.addKeyPressHandler(keyPressHandler);
	}
}
