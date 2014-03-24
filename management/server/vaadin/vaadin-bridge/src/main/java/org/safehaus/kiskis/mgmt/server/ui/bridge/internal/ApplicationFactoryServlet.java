package org.safehaus.kiskis.mgmt.server.ui.bridge.internal;

import com.vaadin.Application;
import com.vaadin.Application.SystemMessages;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;
import org.safehaus.kiskis.mgmt.server.ui.bridge.ApplicationFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

class ApplicationFactoryServlet extends AbstractApplicationServlet {

    private static final long serialVersionUID = 1L;
    private final ApplicationFactory factory;

    ApplicationFactoryServlet(ApplicationFactory factory) {
        this.factory = factory;
    }

    @Override
    protected Application getNewApplication(HttpServletRequest request) throws ServletException {
        return factory.newInstance();
    }

    @Override
    protected Class<? extends Application> getApplicationClass() {
        return null;
    }

    @Override
    protected String getApplicationCSSClassName() {
        return factory.getApplicationCSSClassName();
    }

    @Override
    protected SystemMessages getSystemMessages() {
        SystemMessages messages = factory.getSystemMessages();

        return messages != null ? messages : Application.getSystemMessages();
    }
}