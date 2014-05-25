package org.safehaus.subutai.server.ui.services;

import com.vaadin.ui.Component;

public interface Module {

    public String getName();

    public Component createComponent();

//    public void dispose();
}
