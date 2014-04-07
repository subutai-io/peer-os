package org.safehaus.kiskis.mgmt.ui.monitor;

import com.vaadin.ui.Component;
import org.safehaus.kiskis.mgmt.ui.monitor.view.ModuleComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class Monitor implements Module {

    private static final String MODULE_NAME = "Monitor";

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent();
    }
}
