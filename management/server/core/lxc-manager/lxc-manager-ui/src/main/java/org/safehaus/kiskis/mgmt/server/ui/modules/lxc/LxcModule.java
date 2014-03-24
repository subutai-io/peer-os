package org.safehaus.kiskis.mgmt.server.ui.modules.lxc;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.clone.Cloner;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.manage.Manager;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;

public class LxcModule implements Module {

    public static final String MODULE_NAME = "LXC";
    private AgentManager agentManager;
    private LxcManager lxcManager;
    private static ExecutorService executor;

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setLxcManager(LxcManager lxcManager) {
        this.lxcManager = lxcManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static class ModuleComponent extends CustomComponent {

        public ModuleComponent(AgentManager agentManager, LxcManager lxcManager) {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet commandsSheet = new TabSheet();
            commandsSheet.setStyleName(Runo.TABSHEET_SMALL);
            commandsSheet.setSizeFull();

            commandsSheet.addTab(new Cloner(lxcManager), "Clone");
            commandsSheet.addTab(new Manager(agentManager, lxcManager), "Manage");

            verticalLayout.addComponent(commandsSheet);

            setCompositionRoot(verticalLayout);

        }

    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(agentManager, lxcManager);
    }

}
