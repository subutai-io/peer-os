package org.safehaus.kiskis.mgmt.server.ui.modules.pig;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.*;
import com.vaadin.ui.*;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.ModuleComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.HashMap;
import java.util.Map;

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

        ChainBase base = new ChainBase();
        base.addCommand(new TestCommand("1"));

        Context context = new ContextBase();

        base.execute(context);

        System.out.println("done");
    }

}

class TestCommand implements Command {

    String id;

    TestCommand(String id) {
        this.id = id;
    }

    @Override
    public boolean execute(Context context) throws Exception {

        System.out.println("id: " + id);

        return false;
    }
}
