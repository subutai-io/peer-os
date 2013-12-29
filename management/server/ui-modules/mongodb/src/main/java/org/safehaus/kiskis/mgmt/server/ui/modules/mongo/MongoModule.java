package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.Wizard;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.management.Manager;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoModule implements Module {

    public static final String MODULE_NAME = "MongoDB";

    private static final Logger LOG = Logger.getLogger(MongoModule.class.getName());

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final Wizard wizard;
        private final Manager mongoManager;

        public ModuleComponent() {
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet mongoSheet = new TabSheet();
            mongoSheet.setStyleName(Runo.TABSHEET_SMALL);
            mongoSheet.setSizeFull();
            wizard = new Wizard();
            mongoManager = new Manager();
            mongoSheet.addTab(wizard.getContent(), "Install");
            mongoSheet.addTab(mongoManager.getContent(), "Manage");

            verticalLayout.addComponent(mongoSheet);

            setCompositionRoot(verticalLayout);

        }

        @Override
        public void onCommand(Response response) {
            if (wizard.getSource().equalsIgnoreCase(response.getSource())) {
                wizard.onResponse(response);
            } else if (mongoManager.getSource().equalsIgnoreCase(response.getSource())) {
                mongoManager.onResponse(response);
            }
        }

        @Override
        public String getName() {
            return MODULE_NAME;
        }

    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent();
    }

    public void setModuleService(ModuleService service) {
        try {
            LOG.log(Level.INFO, "{0}: registering with ModuleService", MODULE_NAME);
            service.registerModule(this);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in setModuleService", e);
        }

    }

    public void unsetModuleService(ModuleService service) {
        try {
            service.unregisterModule(this);
            LOG.log(Level.INFO, "{0}: Unregistering with ModuleService", MODULE_NAME);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in unsetModuleService", e);
        }
    }
}
