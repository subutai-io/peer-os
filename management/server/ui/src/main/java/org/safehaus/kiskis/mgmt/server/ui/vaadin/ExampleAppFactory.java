package org.safehaus.kiskis.mgmt.server.ui.vaadin;

import com.vaadin.Application;
import com.vaadin.Application.SystemMessages;
import org.safehaus.kiskis.mgmt.vaadin.bridge.ApplicationFactory;

public class ExampleAppFactory implements ApplicationFactory {

    private final String title;
//    private ResponseStorage broker;

    //
    public ExampleAppFactory(String title) {
        this.title = title;
    }

    @Override
    public String getApplicationCSSClassName() {
        return "Kiskis Management Server UI";
    }

    @Override
    public SystemMessages getSystemMessages() {
        return null;
    }

    @Override
    public Application newInstance() {
        return new ExampleApplication(title);
    }
}