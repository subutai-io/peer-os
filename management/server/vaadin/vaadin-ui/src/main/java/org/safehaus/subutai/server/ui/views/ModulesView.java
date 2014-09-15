/**
 * DISCLAIMER
 *
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 *
 * @author jouni@vaadin.com
 *
 */

package org.safehaus.subutai.server.ui.views;

import com.vaadin.Application;
import com.vaadin.event.LayoutEvents;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.subutai.server.ui.MainUI;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.server.ui.api.PortalModuleListener;
import org.safehaus.subutai.server.ui.api.PortalModuleService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.logging.Logger;

public class ModulesView extends VerticalLayout implements View, PortalModuleListener, HttpServletRequestListener {

    private static final Logger LOG = Logger.getLogger(MainUI.class.getName());
    private TabSheet editors;
    private CssLayout modulesLayout;
    private HashMap<String, PortalModule> modules = new HashMap<>();

    private static ThreadLocal<ModulesView> instance = new ThreadLocal<>();

    private Application application;

    public ModulesView(Application application) {
        this.application = application;
        instance.set(this);
    }

    public static TabSheet getEditor(){
        return instance.get().editors;
    }
    @Override
    public void enter(ViewChangeEvent event) {
        setSizeFull();
        addStyleName("reports");

        addComponent(buildDraftsView());
        getPortalModuleService().addListener(this);
    }

    private static Component buildDraftsView() {
        instance.get().editors = new TabSheet();
        instance.get().editors.setSizeFull();
        instance.get().editors.addStyleName("borderless");
        instance.get().editors.addStyleName("editors");

        instance.get().editors.setCloseHandler(new TabSheet.CloseHandler() {
            @Override
            public void onTabClose(TabSheet components, Component component) {
                instance.get().editors.removeComponent(component);
                instance.get().modules.remove(component.getId());
            }
        });

        VerticalLayout titleAndDrafts = new VerticalLayout();
        titleAndDrafts.setSizeUndefined();
        titleAndDrafts.setCaption("Modules");
        titleAndDrafts.setSpacing(true);
        titleAndDrafts.addStyleName("drafts");
        instance.get().editors.addComponent(titleAndDrafts);

        Label draftsTitle = new Label("Modules");
        draftsTitle.addStyleName("h1");
        draftsTitle.setSizeUndefined();
        titleAndDrafts.addComponent(draftsTitle);
        titleAndDrafts.setComponentAlignment(draftsTitle, Alignment.TOP_CENTER);

        instance.get().modulesLayout = new CssLayout();
        instance.get().modulesLayout.setSizeUndefined();
        instance.get().modulesLayout.addStyleName("catalog");
        titleAndDrafts.addComponent(instance.get().modulesLayout);

        for (PortalModule module : getPortalModuleService().getModules()) {
            addModule(module);
        }

        return instance.get().editors;
    }

    public static PortalModuleService getPortalModuleService() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(PortalModuleService.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(PortalModuleService.class.getName());
            if (serviceReference != null) {
                return PortalModuleService.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

    private static void addModule(final PortalModule module) {
        CssLayout moduleLayout = new CssLayout();
        moduleLayout.setId(module.getId());
        moduleLayout.setWidth(150, Unit.PIXELS);
        moduleLayout.setHeight(200, Unit.PIXELS);
        moduleLayout.addStyleName("create");

        moduleLayout.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent layoutClickEvent) {
                if (!instance.get().modules.containsKey(module.getId())) {
                    autoCreate(module);
                    instance.get().modules.put(module.getId(), module);
                }
            }
        });

        Image image = new Image("", new FileResource(module.getImage()));
        image.setWidth(90, Unit.PERCENTAGE);
        image.setDescription(module.getName());
        moduleLayout.addComponent(image);

        instance.get().modulesLayout.addComponent(moduleLayout);
    }

    public static void autoCreate(PortalModule module) {
        Component component = module.createComponent();
        component.setId(module.getId());
        TabSheet.Tab tab = instance.get().editors.addTab(component);
        tab.setCaption(module.getName());
        tab.setClosable(true);
        instance.get().editors.setSelectedTab(tab);
    }

    @Override
    public void moduleRegistered(PortalModule module) {
        instance.get().addModule(module);
    }

    @Override
    public void moduleUnregistered(PortalModule module) {
    }

    @Override
    public void onRequestStart(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        instance.set(this);
    }

    @Override
    public void onRequestEnd(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        instance.remove();
    }

    public static ModulesView getInstance() {
        return instance.get();
    }
}

