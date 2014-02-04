package org.safehaus.kiskis.mgmt.server.ui.modules.pig;

import com.vaadin.ui.*;

import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class Pig implements Module {

    private static final String MODULE_NAME = "Pig";

    @Override
    public String getName() {
        return Pig.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(MODULE_NAME);
    }
}
