/**
 * DISCLAIMER
 *
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 *
 * @author jouni@vaadin.com
 *
 */

package org.safehaus.subutai.server.ui.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;

public class ReportsView extends HorizontalLayout implements View {

	private TabSheet editors;

	@Override
	public void enter(ViewChangeEvent event) {
		setSizeFull();
		addStyleName("reports");

		addComponent(new Label("Reports"));
	}
}
