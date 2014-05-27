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
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class MonitorView extends VerticalLayout implements View {

    @Override
    public void enter(ViewChangeEvent event) {
        setSizeFull();
        addStyleName("timeline");

        Label header = new Label("Revenue by Movie Title");
        header.addStyleName("h1");
        addComponent(header);
    }
}
