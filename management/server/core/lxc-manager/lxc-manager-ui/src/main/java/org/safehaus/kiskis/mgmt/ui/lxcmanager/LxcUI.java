package org.safehaus.kiskis.mgmt.ui.lxcmanager;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.ui.lxcmanager.clone.Cloner;
import org.safehaus.kiskis.mgmt.ui.lxcmanager.manage.Manager;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;

public class LxcUI implements Module {

    public static final String MODULE_NAME = "LXC";
    private AgentManager agentManager;
    private LxcManager lxcManager;
    private static ExecutorService executor;
    private final static String managerTabCaption = "Manage";

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
            final Manager manager = new Manager(agentManager, lxcManager);
            commandsSheet.addTab(new Cloner(lxcManager), "Clone");
            commandsSheet.addTab(manager, managerTabCaption);

            commandsSheet.addListener(new TabSheet.SelectedTabChangeListener() {

                public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                    TabSheet tabsheet = event.getTabSheet();
                    String caption = tabsheet.getTab(event.getTabSheet().getSelectedTab()).getCaption();
                    if (caption.equals(managerTabCaption)) {
                        manager.getLxcInfo();
                    }
                }
            });

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
