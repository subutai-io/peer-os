package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop;

import com.vaadin.ui.*;

import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.ModuleComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class Sqoop implements Module {

    private static final String MODULE_NAME = "Sqoop";

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(MODULE_NAME);
    }
}
