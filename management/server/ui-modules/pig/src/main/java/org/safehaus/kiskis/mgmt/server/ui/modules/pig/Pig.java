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

        /*
        chain.add(chain2);
        chain.add(new BaseAction("2"));
        chain.add(new BaseAction("3"));

        chain.execute(new HashMap<String, Object>());
        */
    }
}
