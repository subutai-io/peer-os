/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.server.ui.component;

import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * @author dilshat
 */
public abstract class ConcurrentComponent extends VerticalLayout {

	protected void executeUpdate(Runnable update) {
		UI application;
		synchronized (this) {
			application = UI.getCurrent();
			if (application == null) {
				update.run();
				return;
			}
		}
		synchronized (application) {
			update.run();
		}
	}
}
