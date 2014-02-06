package org.safehaus.kiskis.mgmt.server.ui.modules.lucene;

import com.vaadin.ui.*;

import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class Lucene implements Module {

    private static final String MODULE_NAME = "Lucene";

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        //return new ModuleComponent(MODULE_NAME);
        return null;
    }
}
