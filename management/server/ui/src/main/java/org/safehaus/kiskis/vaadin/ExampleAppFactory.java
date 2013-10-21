package org.safehaus.kiskis.vaadin;

import com.vaadin.Application;
import com.vaadin.Application.SystemMessages;
import org.safehaus.kiskis.mgmt.server.broker.impl.ResponseStorage;
import org.safehaus.kiskis.mgmt.vaadin.bridge.ApplicationFactory;

public class ExampleAppFactory implements ApplicationFactory {

    private final String title;
//    private ResponseStorage broker;

//    , ResponseStorage broker
    public ExampleAppFactory(String title) {
        this.title = title;
//        this.broker = broker;
    }

    @Override
    public String getApplicationCSSClassName() {
        return "ExampleApplication";
    }

    @Override
    public SystemMessages getSystemMessages() {
        return null;
    }

    @Override
    public Application newInstance() {
        return new ExampleApplication(title);
    }

//    public void setBroker(ResponseStorage broker) {
//        this.broker = broker;
//    }
}