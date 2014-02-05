package org.safehaus.kiskis.mgmt.server.ui.modules.flume;

import com.vaadin.ui.*;

import org.safehaus.kiskis.mgmt.server.ui.modules.flume.view.ModuleComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class Flume implements Module {

    private static final String MODULE_NAME = "Flume";

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(MODULE_NAME);
    }
}
