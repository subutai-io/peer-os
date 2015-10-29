/**
 * NovaForge(TM) is a web-based forge offering a Collaborative Development and Project Management Environment.
 *
 * Copyright (C) 2007-2012  BULL SAS
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * http://www.gnu.org/licenses/.
 */
package io.subutai.server.ui;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;

import io.subutai.common.settings.Common;
import io.subutai.server.ui.util.HelpManager;
import io.subutai.server.ui.util.HelpOverlay;
import io.subutai.server.ui.views.CoreModulesView;
import io.subutai.server.ui.views.ModulesView;
import io.subutai.server.ui.views.LoginView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


@Theme( "dashboard" )
@SuppressWarnings( "serial" )
@Title( "Subutai" )

public class MainUI extends UI implements ViewChangeListener
{

    private static final Logger LOG = LoggerFactory.getLogger( MainUI.class.getName() );
    private static final ThreadLocal<MainUI> THREAD_LOCAL = new ThreadLocal<>();

    private CssLayout root = new CssLayout();
    private CssLayout menu = new CssLayout();
    private CssLayout content = new CssLayout();
    private HelpManager helpManager;
    private Navigator nav;
    Label username;

    private Map<String, Button> viewNameToMenuButton = new HashMap<>();
    private Map<String, View> routes = ImmutableMap.<String, View>builder().put( "/core", new CoreModulesView() )
                                                   .put( "/modules", new ModulesView() ).build();


    public static MainUI getInstance()
    {
        return THREAD_LOCAL.get();
    }


    private static void setInstance( MainUI application )
    {
        THREAD_LOCAL.set( application );
    }


    @Override
    protected void init( VaadinRequest request )
    {
        setInstance( this );
        helpManager = new HelpManager( this );

        setLocale( Locale.US );

        setContent( root );
        root.addStyleName( "root" );
        root.setSizeFull();

        // Unfortunate to use an actual widget here, but since CSS generated
        // elements can't be transitioned yet, we must
        Label bg = new Label();
        bg.setSizeUndefined();
        bg.addStyleName( "login-bg" );
        root.addComponent( bg );

        // For synchronization issue
        setPollInterval( Common.REFRESH_UI_SEC * 340 );

        buildMainView();
    }


    public Label getUsername()
    {
        return username;
    }


    @Override
    public boolean beforeViewChange( final ViewChangeListener.ViewChangeEvent event )
    {
        helpManager.closeAll();


        LOG.debug( String.format( "View: %s", event.getViewName() ) );

        boolean isAuthenticated = false;

        //if ( !( loginContext instanceof NullSubutaiLoginContext ) )
        {
            try
            {
                //IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
                //isAuthenticated = identityManager != null && identityManager.isAuthenticated();
            }
            catch ( Exception e )
            {
                LOG.error( e.toString(), e );
            }
        }

        if ( isAuthenticated && "".equals( event.getViewName() ) )
        {
            event.getNavigator().navigateTo( "/core" );
            return false;
        }

        if ( !isAuthenticated && !"/login".equals( event.getViewName() ) )
        {
            HelpOverlay w = helpManager.addOverlay( "Welcome to the Subutai",
                    "<p>No username or password is required, just click the ‘Sign In’ button to continue.</p>",
                    "login" );
            w.center();
            getUI().addWindow( w );
            event.getNavigator().navigateTo( "/login" );
            return false;
        }
        return true;
    }


    @Override
    public void afterViewChange( final ViewChangeListener.ViewChangeEvent event )
    {
        View newView = event.getNewView();
        helpManager.showHelpFor( newView );
    }


    private void buildMainView()
    {
        nav = new Navigator( this, content );
        nav.addViewChangeListener( this );
        nav.addView( "/login", new LoginView( this, helpManager ) );
        for ( String route : routes.keySet() )
        {
            nav.addView( route, routes.get( route ) );
        }

        helpManager.closeAll();

        root.addComponent( buildMainLayout() );

        menu.removeAllComponents();

        for ( final String view : new String[] { "core", "modules" } )
        {
            Button.ClickListener navButtonListener = new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    clearMenuSelection();
                    event.getButton().addStyleName( "selected" );
                    String selectedButton = "/" + view;
                    if ( !selectedButton.equals( nav.getState() ) )
                    {
                        nav.navigateTo( "/" + view );
                    }
                }
            };
            Button navButton =
                    new NativeButton( view.substring( 0, 1 ).toUpperCase() + view.substring( 1 ).replace( '-', ' ' ) );
            navButton.setId( view.substring( 0, 1 ).toUpperCase() + view.substring( 1 ).replace( '-', ' ' ) );
            navButton.addStyleName( "icon-" + view );
            navButton.addClickListener( navButtonListener );

            menu.addComponent( navButton );
            viewNameToMenuButton.put( "/" + view, navButton );
        }
        menu.addStyleName( "menu" );
        menu.setHeight( "100%" );

        String currentFragment = Page.getCurrent().getUriFragment();
        if ( currentFragment != null && currentFragment.startsWith( "!" ) )
        {
            currentFragment = currentFragment.substring( 1 );
        }
        if ( currentFragment == null || "".equals( currentFragment ) || "/".equals( currentFragment ) )
        {
            nav.navigateTo( "/core" );
            menu.getComponent( 0 ).addStyleName( "selected" );
            helpManager.showHelpFor( ModulesView.class );
        }
        else
        {
            nav.navigateTo( currentFragment );
            if ( !"/login".equals( currentFragment ) )
            {
                helpManager.showHelpFor( routes.get( currentFragment ) );
                viewNameToMenuButton.get( currentFragment ).addStyleName( "selected" );
            }
        }
    }


    private HorizontalLayout buildMainLayout()
    {
        CssLayout brandingLayout = new CssLayout();
        brandingLayout.addStyleName( "branding" );
        brandingLayout.setHeight( 140, Unit.PIXELS );
        brandingLayout.addComponent( new Image( null, new ThemeResource( "img/subutai.png" ) ) );

        VerticalLayout userLayout = new VerticalLayout();
        userLayout.setSizeUndefined();
        userLayout.addStyleName( "user" );

        Image profilePic = new Image( null, new ThemeResource( "img/profile-pic.png" ) );
        profilePic.setWidth( "34px" );
        userLayout.addComponent( profilePic );

        username = new Label( "unknown" );
        username.setSizeUndefined();
        userLayout.addComponent( username );

        try
        {
            /*
            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );

            if ( identityManager != null )
            {
                User user = identityManager.getUser();

                if ( user != null )
                {
                    username.setValue( user.getUsername() );
                }
            }*/
        }
        catch ( Exception e )
        {
            LOG.error( "Error getting username #buildMainLayout", e );
        }

        MenuBar.Command cmd = new MenuBar.Command()
        {
            @Override
            public void menuSelected( MenuBar.MenuItem selectedItem )
            {
                Notification.show( "Not implemented in this demo" );
            }
        };

        MenuBar settings = new MenuBar();
        MenuBar.MenuItem settingsMenu = settings.addItem( "", null );
        settingsMenu.setStyleName( "icon-cog" );
        settingsMenu.addItem( "Settings", cmd );
        settingsMenu.addItem( "Preferences", cmd );
        settingsMenu.addSeparator();
        settingsMenu.addItem( "My Account", cmd );
        userLayout.addComponent( settings );

        Button.ClickListener exitListener = new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {

                try
                {
                }
                catch ( Exception e )
                {
                    LOG.error( e.toString(), e );
                }

                String contextPath = VaadinService.getCurrentRequest().getContextPath();
                getUI().getPage().setLocation( contextPath );
                LOG.trace( "VaadinSession.close() called" );
            }
        };

        Button exit = new NativeButton( "Exit" );
        exit.addStyleName( "icon-cancel" );
        exit.setDescription( "Sign Out" );
        userLayout.addComponent( exit );

        exit.addClickListener( exitListener );

        VerticalLayout sidebarLayout = new VerticalLayout();
        sidebarLayout.addStyleName( "sidebar" );
        sidebarLayout.setWidth( 140, Unit.PIXELS );
        sidebarLayout.setHeight( "100%" );
        sidebarLayout.addComponent( brandingLayout );
        sidebarLayout.addComponent( menu );
        sidebarLayout.setExpandRatio( menu, 1 );
        sidebarLayout.addComponent( userLayout );

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.addStyleName( "main-view" );
        mainLayout.addComponent( sidebarLayout );
        mainLayout.addComponent( content );
        content.setSizeFull();
        content.addStyleName( "view-content" );
        mainLayout.setExpandRatio( content, 1 );

        return mainLayout;
    }


    private void clearMenuSelection()
    {
        for ( Component next : menu )
        {
            if ( next instanceof NativeButton )
            {
                next.removeStyleName( "selected" );
            }
            else if ( next instanceof DragAndDropWrapper )
            {
                // Wow, this is ugly (even uglier than the rest of the code)
                ( ( DragAndDropWrapper ) next ).iterator().next().removeStyleName( "selected" );
            }
        }
    }
}
