package org.safehaus.kiskis.mgmt.server.ui.services;

import com.vaadin.ui.Component;

public interface Module {

    public String getName();

    public Component createComponent();

}
