package org.safehaus.kiskis.mgmt.server.ui.modules.client;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.SomeApi;

public class ClientModule implements Module {

    public static final String MODULE_NAME = "Client";
    private static final Logger LOG = Logger.getLogger(ClientModule.class.getName());
    private SomeApi someapi;

    public void setSomeApi(SomeApi someapi) {
        this.someapi = someapi;
    }

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        public ModuleComponent(final SomeApi someapi) {

            setSizeFull();
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet sheet = new TabSheet();
            sheet.setStyleName(Runo.TABSHEET_SMALL);
            sheet.setSizeFull();
            final TextArea t = new TextArea();
            Button b = new Button("Say hello");
            b.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    String hello = someapi.sayHello("babahos");
                    t.setValue(hello);

                }
            });

            verticalLayout.addComponent(sheet);

            setCompositionRoot(verticalLayout);
        }

        @Override
        public void onCommand(Response response) {
        }

        @Override
        public String getName() {
            return MODULE_NAME;
        }

        public Iterable<Agent> getLxcList() {
            return MgmtApplication.getSelectedAgents();
        }

    }

    @Override
    public String getName() {
        return ClientModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(someapi);
    }

}
