package io.subutai.server.ui.views;


import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.server.ui.MainUI;
import io.subutai.server.ui.api.PortalModule;
import io.subutai.server.ui.api.PortalModuleListener;
import io.subutai.server.ui.api.PortalModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class CoreModulesView extends VerticalLayout implements View, PortalModuleListener
{

    private static final Logger LOG = LoggerFactory.getLogger( MainUI.class.getName() );
    private TabSheet editors;
    private CssLayout modulesLayout;
    private HashMap<String, PortalModule> modules = new HashMap<>();
    private HashMap<String, AbstractLayout> moduleViews = new HashMap<>();
    private static PortalModuleService portalModuleService = null;
    private static final ServiceLocator serviceLocator = new ServiceLocator();


    public CoreModulesView()
    {
        setSizeFull();
        addStyleName( "reports" );

        addComponent( buildDraftsView() );
        getPortalModuleService().addListener( this );
    }


    @Override
    public void enter( ViewChangeListener.ViewChangeEvent event )
    {
        try
        {
            for ( final Map.Entry<String, AbstractLayout> entry : moduleViews.entrySet() )
            {
                AbstractLayout layout = moduleViews.get( entry.getKey() );
                if ( layout != null )
                {
                    layout.setVisible( false );
                }
            }

            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
            User user = identityManager.getUser();
            for ( final Role role : user.getRoles() )
            {
                for ( final PortalModuleScope module : role.getAccessibleModules() )
                {
                    AbstractLayout layout = moduleViews.get( module.getModuleKey() );
                    if ( layout != null )
                    {
                        layout.setVisible( true );
                    }
                }
            }
        }
        catch ( NamingException e )
        {
            LOG.error( "Error getting identityManager service", e );
        }

        LOG.debug( "User entered CoreModulesView" );
    }


    private Component buildDraftsView()
    {
        editors = new TabSheet();
        editors.setSizeFull();
        editors.addStyleName( "borderless" );
        editors.addStyleName( "editors" );

        editors.setCloseHandler( new TabSheet.CloseHandler()
        {
            @Override
            public void onTabClose( TabSheet components, Component component )
            {
                editors.removeComponent( component );
                modules.remove( component.getId() );
            }
        } );

        VerticalLayout titleAndDrafts = new VerticalLayout();
        titleAndDrafts.setSizeUndefined();
        titleAndDrafts.setCaption( "Modules" );
        titleAndDrafts.setSpacing( true );
        titleAndDrafts.addStyleName( "drafts" );
        editors.addComponent( titleAndDrafts );

        Label draftsTitle = new Label( "Core modules" );
        draftsTitle.addStyleName( "h1" );
        draftsTitle.setSizeUndefined();
        titleAndDrafts.addComponent( draftsTitle );
        titleAndDrafts.setComponentAlignment( draftsTitle, Alignment.TOP_CENTER );

        modulesLayout = new CssLayout();
        modulesLayout.setSizeUndefined();
        modulesLayout.addStyleName( "catalog" );
        titleAndDrafts.addComponent( modulesLayout );

        for ( PortalModule module : getPortalModuleService().getCoreModules() )
        {
            addModule( module );
        }

        return editors;
    }


    public static PortalModuleService getPortalModuleService()
    {
        if ( portalModuleService == null )
        {
            // get bundle instance via the OSGi Framework Util class
            BundleContext ctx = FrameworkUtil.getBundle( PortalModuleService.class ).getBundleContext();
            if ( ctx != null )
            {
                ServiceReference serviceReference = ctx.getServiceReference( PortalModuleService.class.getName() );
                if ( serviceReference != null )
                {
                    portalModuleService = PortalModuleService.class.cast( ctx.getService( serviceReference ) );
                    //                    return PortalModuleService.class.cast( ctx.getService( serviceReference ) );
                }
            }
        }
        return portalModuleService;
    }


    private void addModule( final PortalModule module )
    {

        ModuleView moduleView = new ModuleView( module, new ModuleView.ModuleViewListener()
        {
            @Override
            public void OnModuleClick( PortalModule module )
            {
                if ( !modules.containsKey( module.getId() ) )
                {
                    autoCreate( module );
                    modules.put( module.getId(), module );
                }
            }
        } );
        moduleViews.put( module.getId(), moduleView );
        modulesLayout.addComponent( moduleView );
    }


    public void autoCreate( PortalModule module )
    {
        Component component = module.createComponent();
        component.setId( module.getId() );
        TabSheet.Tab tab = editors.addTab( component );
        tab.setCaption( module.getName() );
        tab.setClosable( true );
        editors.setSelectedTab( tab );
    }


    @Override
    public void moduleRegistered( PortalModule module )
    {
        if ( module != null )
        {
            if ( module.isCorePlugin() )
            {
                addModule( module );
            }
        }
    }


    @Override
    public void moduleUnregistered( PortalModule module )
    {
        removeModule( module );
    }


    @Override
    public void loadDependentModule( final String moduleId )
    {
        ModuleView moduleView = ( ModuleView ) getModuleViews().get( moduleId );
        if ( moduleView == null )
        {
            return;
        }
        moduleView.addModuleTab();
    }


    private void removeModule( PortalModule module )
    {
        ModuleView moduleView = ( ModuleView ) moduleViews.get( module.getId() );
        if ( moduleView != null )
        {
            modulesLayout.removeComponent( moduleView );
        }
    }


    public HashMap<String, AbstractLayout> getModuleViews()
    {
        return moduleViews;
    }
}