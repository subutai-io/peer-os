package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.*;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleNotifier;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

@SuppressWarnings("serial")
public class MgmtApplication extends Application implements ModuleServiceListener, HttpServletRequestListener {

    private static final Logger LOG = Logger.getLogger(MgmtApplication.class.getName());
    private static final ThreadLocal<MgmtApplication> threadLocal = new ThreadLocal<MgmtApplication>();
    private final ModuleNotifier moduleNotifier;
    private final AgentManager agentManager;
    private final CommandManager commandManager;
    private Window window;
    private Set<Agent> selectedAgents = new HashSet<Agent>();
    private MgmtAgentManager agentList;

    public MgmtApplication(String title, AgentManager agentManager) {
        this.agentManager = agentManager;
        this.moduleNotifier = ServiceLocator.getService(ModuleNotifier.class);
        this.commandManager = ServiceLocator.getService(CommandManager.class);
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

            window = new Window(title);
            setMainWindow(window);

            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
            horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
            layout.addComponent(horizontalSplit);

            layout.setExpandRatio(horizontalSplit, 1);
            horizontalSplit.setSplitPosition(200, Sizeable.UNITS_PIXELS);

            agentList = new MgmtAgentManager(agentManager);
            //add listener
            agentManager.addListener(agentList);
            Panel panel = new Panel();
            panel.addComponent(agentList);
            panel.setSizeFull();
            horizontalSplit.setFirstComponent(panel);

            tabs = new TabSheet();
            tabs.setSizeFull();
            tabs.setImmediate(true);
            for (Module module : moduleNotifier.getModules()) {
                Component component = module.createComponent();
                tabs.addTab(component, module.getName(), null);
                if (component instanceof CommandListener) {
                    commandManager.addListener((CommandListener) component);
                }
            }
            horizontalSplit.setSecondComponent(tabs);

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
        } catch (Exception ex) {
        } finally {
        }
    }

    @Override
    public void close() {
        try {
            super.close();
            agentManager.removeListener(agentList);
            moduleNotifier.removeListener(this);
            //dispose all modules     
            Iterator<Component> it = tabs.getComponentIterator();
            while (it.hasNext()) {
                Component component = it.next();
                if (component instanceof CommandListener) {
                    commandManager.removeListener((CommandListener) component);
                }
            }
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
            if (component instanceof CommandListener) {
                commandManager.addListener((CommandListener) component);
            }
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
                    tabs.removeComponent(component);
                    if (component instanceof CommandListener) {
                        commandManager.removeListener((CommandListener) component);
                    }
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

    public static Label createImage(String imageName, int imageWidth, int imageHeight) {
        Label image = new Label(
                String.format("<img src='http://%s:%s/%s' />", MgmtApplication.APP_URL, Common.WEB_SERVER_PORT, imageName));
        image.setContentMode(Label.CONTENT_XHTML);
        image.setWidth(imageWidth, Sizeable.UNITS_PIXELS);
        image.setHeight(imageHeight, Sizeable.UNITS_PIXELS);
        return image;
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

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in showConfirmationDialog", e);
        }
    }

    public static void addCustomWindow(Window window) {
        try {
            if (getInstance() != null && window != null) {
                getInstance().getMainWindow().addWindow(window);
                window.bringToFront();
            }
        } catch (Exception e) {
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

}
