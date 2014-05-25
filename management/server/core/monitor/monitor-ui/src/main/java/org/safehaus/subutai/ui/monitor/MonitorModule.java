package org.safehaus.subutai.ui.monitor;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.monitor.Monitor;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class MonitorModule implements Module {

    private static final String MODULE_NAME = "Monitor";

    private Monitor monitor;

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public Component createComponent() {
        return new ModuleView(monitor);
    }
}
