package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class MgmtApplication extends Application implements ModuleServiceListener, HttpServletRequestListener {

    private static final Logger LOG = Logger.getLogger(MgmtApplication.class.getName());
    private ModuleServiceListener app;
    private final ModuleService moduleService;
    private Window window;
    private static final ThreadLocal<MgmtApplication> threadLocal = new ThreadLocal<MgmtApplication>();
    private Set<Agent> selectedAgents = new HashSet<Agent>();
    private final AgentManagerInterface agentManagerService;
    private MgmtAgentManager agentManager;

    public MgmtApplication(String title, ModuleService moduleService, AgentManagerInterface agentManagerService) {
        this.moduleService = moduleService;
        this.agentManagerService = agentManagerService;
        this.title = title;
    }
    private final String title;
    private TabSheet tabs;
    public static String APP_URL;

    @Override
    public void init() {
        APP_URL = getURL().getHost();
        setInstance(this);
        try {
            setTheme(Runo.themeName());

            app = this;
            window = new Window(title);
            setMainWindow(window);

            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
            horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
            layout.addComponent(horizontalSplit);

            layout.setExpandRatio(horizontalSplit, 1);
            horizontalSplit.setSplitPosition(200, Sizeable.UNITS_PIXELS);

            agentManager = new MgmtAgentManager(agentManagerService);
            //add listener
            agentManagerService.addListener(agentManager);
            Panel panel = new Panel();
            panel.addComponent(agentManager);
            panel.setSizeFull();
            horizontalSplit.setFirstComponent(panel);

            tabs = new TabSheet();
            tabs.setSizeFull();
            tabs.setImmediate(true);
            for (Module module : moduleService.getModules()) {
                tabs.addTab(module.createComponent(), module.getName(), null);
            }
            horizontalSplit.setSecondComponent(tabs);

            getMainWindow().setContent(layout);
            //add listener
            moduleService.addListener(this);
            getMainWindow().addListener(new Window.CloseListener() {
                @Override
                public void windowClose(Window.CloseEvent e) {
                    try {
                        if (moduleService != null) {
                            agentManagerService.removeListener(agentManager);
                            moduleService.removeListener(app);
                            LOG.log(Level.INFO, "Removing app as module listener");
                        }
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Error in windowClose", ex);
                    }
                }
            });
            //
            final ProgressIndicator indicator
                    = new ProgressIndicator(new Float(0.0));
            indicator.setPollingInterval(Common.REFRESH_UI_SEC * 1000);
            indicator.setWidth("1px");
            indicator.setHeight("1px");
            getMainWindow().addComponent(indicator);
            //            
//            getMainWindow().executeJavaScript("var url = location.protocol+'//'+location.hostname;");
        } catch (Exception ex) {
        } finally {
        }
    }

    @Override
    public void close() {
        try {
            super.close();
            agentManagerService.removeListener(agentManager);
            moduleService.removeListener(app);
            LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Application closing, removing module service listener");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Kiskis Management Vaadin UI: Error closing", e);
        }
    }

    @Override
    public void moduleRegistered(ModuleService source, Module module) {
        try {
            LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Module registered, adding tab");
            tabs.addTab(module.createComponent(), module.getName(), null);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Kiskis Management Vaadin UI: Error registering module{0}", e);
        }
    }

    @Override
    public void moduleUnregistered(ModuleService source, Module module) {
        try {
            LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Module unregistered, removing tab");
            Iterator<Component> it = tabs.getComponentIterator();
            while (it.hasNext()) {
                Component c = it.next();
                if (tabs.getTab(c).getCaption().equals(module.getName())) {
                    tabs.removeComponent(c);
                    return;
                }
            }
            module.dispose();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Kiskis Management Vaadin UI: Error unregistering module{0}", e);
        }
    }

    public static MgmtApplication getInstance() {
        return threadLocal.get();
    }

    public static void setInstance(MgmtApplication application) {
        threadLocal.set(application);
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        MgmtApplication.setInstance(this);
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        threadLocal.remove();
    }

    public static Set<Agent> getSelectedAgents() {
        if (getInstance() != null) {
            return getInstance().selectedAgents;
        }
        return null;
    }

    public static void setSelectedAgents(Set<Agent> agents) {
        if (getInstance() != null && agents != null) {
            getInstance().selectedAgents = agents;
        }
    }

}
