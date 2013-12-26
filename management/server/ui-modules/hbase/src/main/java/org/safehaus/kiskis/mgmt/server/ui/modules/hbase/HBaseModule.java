package org.safehaus.kiskis.mgmt.server.ui.modules.hbase;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HBaseModule implements Module {

    public static final String MODULE_NAME = "HBase";

    private static final Logger LOG = Logger.getLogger(HBaseModule.class.getName());
    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        public ModuleComponent() {
        }

        @Override
        public void onCommand(Response response) {
        }

        @Override
        public String getName() {
            return HBaseModule.MODULE_NAME; 
        }

    }

    @Override
    public String getName() {
        return HBaseModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        try {
            component = new ModuleComponent();
            ServiceLocator.getService(CommandManagerInterface.class).addListener(component);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in createComponent", e);
        }
        return component;
    }

    @Override
    public void dispose() {
        try {
            ServiceLocator.getService(CommandManagerInterface.class).removeListener(component);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in dispose", e);
        }
    }

    public void setModuleService(ModuleService service) {
        if (service != null) {
            LOG.log(Level.INFO, "{0} registering with ModuleService", MODULE_NAME);
            service.registerModule(this);
        }
    }

    public void unsetModuleService(ModuleService service) {
        if (service != null) {
            service.unregisterModule(this);
            LOG.log(Level.INFO, "{0} Unregistering with ModuleService", MODULE_NAME);
        }
    }
}
