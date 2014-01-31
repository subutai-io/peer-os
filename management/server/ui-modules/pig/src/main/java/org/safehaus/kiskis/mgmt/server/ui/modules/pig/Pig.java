package org.safehaus.kiskis.mgmt.server.ui.modules.pig;

import com.vaadin.ui.*;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.ModuleComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class Pig implements Module {

    public static final String MODULE_NAME = "Pig";

    @Override
    public String getName() {
        return Pig.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent();
    }

    public static void main(String args[]) throws Exception {

        String s = "ii  ksks-bridge                     1.0.0-1                      This package configures Br0 bridge for Physical machines using eth0 interface.";

        System.out.println(s.matches(".*ksks.*"));
    }
}
