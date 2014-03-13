package org.safehaus.kiskis.mgmt.server.ui.modules.logs;

import com.vaadin.ui.*;
import java.util.List;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.SomeApi;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

public class LogsModule implements Module {

    public static final String MODULE_NAME = "Logs";
    private static final Logger LOG = Logger.getLogger(LogsModule.class.getName());
    private SomeApi someApi;
    private DbManager dbManager;

    public void setSomeApi(SomeApi someApi) {
        this.someApi = someApi;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public static class ModuleComponent extends CustomComponent {

        public ModuleComponent(final SomeApi someApi, final DbManager dbManager) {

            setSizeFull();
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

//            TabSheet sheet = new TabSheet();
//            sheet.setStyleName(Runo.TABSHEET_SMALL);
//            sheet.setSizeFull();
            final TextArea t = new TextArea();
            t.setSizeFull();
            Button getlogs = new Button("Get System Logs");
            getlogs.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

//                    String hello = someApi.sayHello("logs");
                    List<String> list = dbManager.getLogs();
                    StringBuilder logs = new StringBuilder();
                    for (String log : list) {
                        logs.append(log);
                    }
                    t.setValue(logs);
                }
            });

            Button putlogs = new Button("Put logs");
            putlogs.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    String cql = "insert into logs (id, log) values ("
                            + System.currentTimeMillis()
                            + ",logs)";
                    dbManager.executeCqlQuery(cql);
                }
            });

            verticalLayout.addComponent(putlogs);
            verticalLayout.addComponent(getlogs);
            verticalLayout.addComponent(t);

            setCompositionRoot(verticalLayout);
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
        return new ModuleComponent(someApi, dbManager);
    }

}
