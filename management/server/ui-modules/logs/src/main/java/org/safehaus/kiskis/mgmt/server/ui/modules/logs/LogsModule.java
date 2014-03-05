package org.safehaus.kiskis.mgmt.server.ui.modules.logs;

import com.vaadin.ui.*;
import java.util.List;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.SomeApi;

public class LogsModule implements Module {

    public static final String MODULE_NAME = "Logs";
    private static final Logger LOG = Logger.getLogger(LogsModule.class.getName());
    private SomeApi someApi;

    public SomeApi getSomeApi() {
        return someApi;
    }

    public void setSomeApi(SomeApi someApi) {
        this.someApi = someApi;
    }

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        public ModuleComponent(final SomeApi someApi) {

            setSizeFull();
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

//            TabSheet sheet = new TabSheet();
//            sheet.setStyleName(Runo.TABSHEET_SMALL);
//            sheet.setSizeFull();
            final TextArea t = new TextArea();
            t.setSizeFull();
            Button b = new Button("Get System Logs");
            b.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

//                    String hello = someApi.sayHello("logs");
                    List<String> list = someApi.getLogs();
                    StringBuilder logs = new StringBuilder();
                    for (String log : list) {
                        logs.append(log);
                    }
                    t.setValue(logs);
                }
            });

            verticalLayout.addComponent(b);
            verticalLayout.addComponent(t);

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
        return LogsModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(someApi);
    }

}
