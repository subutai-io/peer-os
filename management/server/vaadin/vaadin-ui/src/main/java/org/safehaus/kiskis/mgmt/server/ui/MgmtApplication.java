package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.themes.Runo;
import java.util.Collections;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.MainUISelectedTabChangeListener;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleNotifier;
import org.safehaus.kiskis.mgmt.shared.protocol.Disposable;

@SuppressWarnings("serial")
public class MgmtApplication extends Application implements ModuleServiceListener, HttpServletRequestListener {

    private static final Logger LOG = Logger.getLogger(MgmtApplication.class.getName());
    private static final ThreadLocal<MgmtApplication> threadLocal = new ThreadLocal<MgmtApplication>();
    private final ModuleNotifier moduleNotifier;
    private final AgentManager agentManager;
    private final TaskRunner taskRunner;
    private final Tracker tracker;
    private Window window;
    private Set<Agent> selectedAgents = new HashSet<Agent>();
//    private MgmtAgentManager agentList;

    public MgmtApplication(String title, AgentManager agentManager, TaskRunner taskRunner, Tracker tracker, ModuleNotifier moduleNotifier) {
        this.agentManager = agentManager;
        this.taskRunner = taskRunner;
        this.tracker = tracker;
        this.moduleNotifier = moduleNotifier;
        this.title = title;
    }
    private final String title;
    private TabSheet tabs;
    private static String APP_URL;

    public static String getAPP_URL() {
        return APP_URL;
    }

    @Override
    public void init() {
        APP_URL = getURL().getHost();
        setInstance(this);
        try {
            setTheme(Runo.themeName());

            window = new Window(title);
            setMainWindow(window);

            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
//            HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
//            horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);

//            layout.setExpandRatio(horizontalSplit, 1);
//            horizontalSplit.setSplitPosition(200, Sizeable.UNITS_PIXELS);
//            agentList = new MgmtAgentManager(agentManager, true);
            //add listener
//            horizontalSplit.setFirstComponent(agentList);
            tabs = new TabSheet();
            tabs.setSizeFull();
            tabs.setImmediate(true);
            for (Module module : moduleNotifier.getModules()) {
                Component component = module.createComponent();
                tabs.addTab(component, module.getName(), null);
            }
            layout.addComponent(tabs);
//            horizontalSplit.setSecondComponent(tabs);

            getMainWindow().setContent(layout);
            //add listener
            moduleNotifier.addListener(this);
            getMainWindow().addListener(new Window.CloseListener() {
                @Override
                public void windowClose(Window.CloseEvent e) {
                    close();
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

            tabs.addListener(new TabSheet.SelectedTabChangeListener() {

                public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                    TabSheet tabsheet = event.getTabSheet();
                    Tab selectedTab = tabsheet.getTab(event.getTabSheet().getSelectedTab());
                    notifyTabListeners(selectedTab);
                }
            });

            if (tabs.getSelectedTab() != null) {
                notifyTabListeners(tabs.getTab(tabs.getSelectedTab()));
            }
        } catch (Exception ex) {
        } finally {
        }
    }

    private void notifyTabListeners(Tab selectedTab) {
        Iterator<Component> it = tabs.getComponentIterator();
        while (it.hasNext()) {
            Component component = it.next();
            if (component instanceof MainUISelectedTabChangeListener) {
                try {
                    ((MainUISelectedTabChangeListener) component).selectedTabChanged(selectedTab);
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void close() {
        try {
            super.close();
            Iterator<Component> it = tabs.getComponentIterator();
            while (it.hasNext()) {
                Component component = it.next();
                if (component instanceof Disposable) {
                    try {
                        ((Disposable) component).dispose();
                    } catch (Exception e) {
                    }
                }
            }
//            agentList.dispose();
            moduleNotifier.removeListener(this);
            LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Application closing, removing module service listener");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Kiskis Management Vaadin UI: Error closing", e);
        }
    }

    @Override
    public void moduleRegistered(Module module) {
        try {
            LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Module registered, adding tab");
            Component component = module.createComponent();
            tabs.addTab(component, module.getName(), null);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Kiskis Management Vaadin UI: Error registering module{0}", e);
        }
    }

    @Override
    public void moduleUnregistered(Module module) {
        try {
            LOG.log(Level.INFO, "Kiskis Management Vaadin UI: Module unregistered, removing tab");
            Iterator<Component> it = tabs.getComponentIterator();
            while (it.hasNext()) {
                Component component = it.next();
                if (tabs.getTab(component).getCaption().equals(module.getName())) {
                    if (component instanceof Disposable) {
                        try {
                            ((Disposable) component).dispose();
                        } catch (Exception e) {
                        }
                    }
                    tabs.removeComponent(component);
                    return;
                }
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Kiskis Management Vaadin UI: Error unregistering module{0}", e);
        }
    }

    private static MgmtApplication getInstance() {
        return threadLocal.get();
    }

    private static void setInstance(MgmtApplication application) {
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
            return Collections.unmodifiableSet(getInstance().selectedAgents);
        }
        return new HashSet<Agent>();
    }

    static void setSelectedAgents(Set<Agent> agents) {
        if (getInstance() != null && agents != null) {
            getInstance().selectedAgents = agents;
        }
    }

    static void clearSelectedAgents() {
        if (getInstance() != null) {
            getInstance().selectedAgents.clear();
        }
    }

    public static void showConfirmationDialog(final String caption, final String question,
            final String okLabel, final String cancelLabel, final ConfirmationDialogCallback callback) {
        try {
            if (getInstance() != null) {
                final ConfirmationDialog cd = new ConfirmationDialog(
                        caption,
                        question,
                        okLabel, cancelLabel, callback);
                getInstance().getMainWindow().addWindow(cd);
                cd.bringToFront();
            }

        } catch (IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "Error in showConfirmationDialog", e);
        } catch (NullPointerException e) {
            LOG.log(Level.SEVERE, "Error in showConfirmationDialog", e);
        }
    }

    public static void addCustomWindow(Window window) {
        try {
            if (getInstance() != null && window != null) {
                getInstance().getMainWindow().addWindow(window);
                window.bringToFront();
            }
        } catch (IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "Error in addCustomWindow", e);
        } catch (NullPointerException e) {
            LOG.log(Level.SEVERE, "Error in addCustomWindow", e);
        }
    }

    public static void removeCustomWindow(Window window) {
        try {
            if (getInstance() != null && window != null) {
                getInstance().getMainWindow().removeWindow(window);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in removeCustomWindow", e);
        }
    }

    public static MgmtAgentManager createAgentTree() {
        if (getInstance() != null) {
            return new MgmtAgentManager(getInstance().agentManager, false);
        }
        return null;
    }

    public static Window createTerminalWindow(final Set<Agent> agents) {
        if (getInstance() != null) {
            return new TerminalWindow(agents, getInstance().taskRunner, getInstance().agentManager);
        }
        return null;
    }

    public static Window createProgressWindow(String source, UUID trackID) {
        if (getInstance() != null) {
            return new ProgressWindow(getInstance().tracker, trackID, source);
        }
        return null;
    }

    public static void showProgressWindow(String source, UUID trackID, final Window.CloseListener closeCallback) {
        Window progressWindow = MgmtApplication.createProgressWindow(source, trackID);
        MgmtApplication.addCustomWindow(progressWindow);
        if (closeCallback != null) {
            progressWindow.addListener(new Window.CloseListener() {

                @Override
                public void windowClose(Window.CloseEvent e) {
                    closeCallback.windowClose(e);
                }
            });
        }
    }

}
