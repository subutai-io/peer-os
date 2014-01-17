package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.Wizard;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage.Manager2;

public class MongoModule implements Module {

    public static final String MODULE_NAME = "MongoDB";

    private static final Logger LOG = Logger.getLogger(MongoModule.class.getName());

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final Wizard wizard;
        private final Manager2 manager;

        public ModuleComponent() {
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet mongoSheet = new TabSheet();
            mongoSheet.setStyleName(Runo.TABSHEET_SMALL);
            mongoSheet.setSizeFull();
            wizard = new Wizard();
            manager = new Manager2();
            mongoSheet.addTab(wizard.getContent(), "Install");
            mongoSheet.addTab(manager.getContent(), "Manage");

            verticalLayout.addComponent(mongoSheet);

            setCompositionRoot(verticalLayout);

        }

        @Override
        public void onCommand(Response response) {
            wizard.onResponse(response);
            manager.onResponse(response);
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

}
