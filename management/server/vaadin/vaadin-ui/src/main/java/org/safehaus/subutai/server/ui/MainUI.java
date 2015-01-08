/**
 * NovaForge(TM) is a web-based forge offering a Collaborative Development and
 * Project Management Environment.
 *
 * Copyright (C) 2007-2012  BULL SAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.safehaus.subutai.server.ui;


import java.util.HashMap;
import java.util.Locale;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.server.ui.util.HelpManager;
import org.safehaus.subutai.server.ui.util.HelpOverlay;
import org.safehaus.subutai.server.ui.views.CoreModulesView;
import org.safehaus.subutai.server.ui.views.ModulesView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
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
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


@Theme( "dashboard" )
@SuppressWarnings( "serial" )
@Title( "Subutai" )

public class MainUI extends UI
{

    private static final Logger LOG = LoggerFactory.getLogger( MainUI.class.getName() );
    private static final ThreadLocal<MainUI> threadLocal = new ThreadLocal<>();

    private CssLayout root = new CssLayout();
    private CssLayout menu = new CssLayout();
    private CssLayout content = new CssLayout();
    private VerticalLayout loginLayout;
    private HelpManager helpManager;
    private Navigator nav;

    private String username = "administrator";

    private CoreModulesView coreModulesView;
    private ModulesView modulesView;

    private HashMap<String, Button> viewNameToMenuButton = new HashMap<>();
    private HashMap<String, View> routes = new HashMap<String, View>()
    {
        {
            coreModulesView = new CoreModulesView();
            modulesView = new ModulesView();
            put( "/core", coreModulesView );
            put( "/modules", modulesView );
        }
    };


    public static MainUI getInstance()
    {
        return threadLocal.get();
    }


    private static void setInstance( MainUI application )
    {
        threadLocal.set( application );
    }


    @Override
    protected void init( VaadinRequest request )
    {
        setInstance( this );
        helpManager = new HelpManager( this );
        VaadinService.getCurrentRequest().getWrappedSession().setAttribute( "username", username );

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

        buildLoginView( false );
    }


    private void buildLoginView( boolean exit )
    {
        if ( exit )
        {
            root.removeAllComponents();
        }
        helpManager.closeAll();
        HelpOverlay w = helpManager.addOverlay( "Welcome to the Subutai",
                "<p>No username or password is required, just click the ‘Sign In’ button to continue.</p>", "login" );
        w.center();
        addWindow( w );

        addStyleName( "login" );

        loginLayout = new VerticalLayout();
        loginLayout.setSizeFull();
        loginLayout.addStyleName( "login-layout" );
        root.addComponent( loginLayout );

        final CssLayout loginPanel = new CssLayout();
        loginPanel.addStyleName( "login-panel" );

        HorizontalLayout labels = new HorizontalLayout();
        labels.setWidth( "100%" );
        labels.setMargin( true );
        labels.addStyleName( "labels" );
        loginPanel.addComponent( labels );

        Label welcome = new Label( "Welcome" );
        welcome.setSizeUndefined();
        welcome.addStyleName( "h4" );
        labels.addComponent( welcome );
        labels.setComponentAlignment( welcome, Alignment.MIDDLE_LEFT );

        Label title = new Label( "Subutai" );
        title.setSizeUndefined();
        title.addStyleName( "h2" );
        title.addStyleName( "light" );
        labels.addComponent( title );
        labels.setComponentAlignment( title, Alignment.MIDDLE_RIGHT );

        HorizontalLayout fields = new HorizontalLayout();
        fields.setSpacing( true );
        fields.setMargin( true );
        fields.addStyleName( "fields" );

        final TextField username = new TextField( "Username" );
        username.focus();
        fields.addComponent( username );

        final PasswordField password = new PasswordField( "Password" );
        fields.addComponent( password );

        final Button signin = new Button( "Sign In" );
        signin.addStyleName( "default" );
        fields.addComponent( signin );
        fields.setComponentAlignment( signin, Alignment.BOTTOM_LEFT );

        final ShortcutListener enter = new ShortcutListener( "Sign In", ShortcutAction.KeyCode.ENTER, null )
        {
            @Override
            public void handleAction( Object sender, Object target )
            {
                signin.click();
            }
        };

        signin.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( username.getValue() != null && username.getValue().equals( "" ) && password.getValue() != null
                        && password.getValue().equals( "" ) )
                {
                    signin.removeShortcutListener( enter );
                    buildMainView();
                }
                else
                {
                    if ( loginPanel.getComponentCount() > 2 )
                    {
                        // Remove the previous error message
                        loginPanel.removeComponent( loginPanel.getComponent( 2 ) );
                    }
                    // Add new error message
                    Label error = new Label( "Wrong username or password. <span>Hint: try empty values</span>",
                            ContentMode.HTML );
                    error.addStyleName( "error" );
                    error.setSizeUndefined();
                    error.addStyleName( "light" );
                    // Add animation
                    error.addStyleName( "v-animate-reveal" );
                    loginPanel.addComponent( error );
                    username.focus();
                }
            }
        } );

        signin.addShortcutListener( enter );

        loginPanel.addComponent( fields );

        loginLayout.addComponent( loginPanel );
        loginLayout.setComponentAlignment( loginPanel, Alignment.MIDDLE_CENTER );
    }


    private void buildMainView()
    {
        nav = new Navigator( this, content );

        for ( String route : routes.keySet() )
        {
            nav.addView( route, routes.get( route ) );
        }

        helpManager.closeAll();
        removeStyleName( "login" );
        root.removeComponent( loginLayout );

        root.addComponent( new HorizontalLayout()
        {
            {
                setSizeFull();
                addStyleName( "main-view" );
                addComponent( new VerticalLayout()
                {
                    // Sidebar
                    {
                        addStyleName( "sidebar" );
                        setWidth( 140, Unit.PIXELS );
                        setHeight( "100%" );

                        // Branding element
                        addComponent( new CssLayout()
                        {
                            {
                                addStyleName( "branding" );
                                setHeight( 140, Unit.PIXELS );
                                addComponent( new Image( null, new ThemeResource( "img/subutai.png" ) ) );
                            }
                        } );

                        // Main menu
                        addComponent( menu );
                        setExpandRatio( menu, 1 );

                        // User menu
                        addComponent( new VerticalLayout()
                        {
                            {
                                setSizeUndefined();
                                addStyleName( "user" );
                                Image profilePic = new Image( null, new ThemeResource( "img/profile-pic.png" ) );
                                profilePic.setWidth( "34px" );
                                addComponent( profilePic );
                                Label userName = new Label( username );
                                userName.setSizeUndefined();
                                addComponent( userName );

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
                                addComponent( settings );

                                Button exit = new NativeButton( "Exit" );
                                exit.addStyleName( "icon-cancel" );
                                exit.setDescription( "Sign Out" );
                                addComponent( exit );
                                exit.addClickListener( new Button.ClickListener()
                                {
                                    @Override
                                    public void buttonClick( Button.ClickEvent event )
                                    {
                                        buildLoginView( true );
                                    }
                                } );
                            }
                        } );
                    }
                } );
                // Content
                addComponent( content );
                content.setSizeFull();
                content.addStyleName( "view-content" );
                setExpandRatio( content, 1 );
            }
        } );

        menu.removeAllComponents();

        for ( final String view : new String[] { "core", "modules" } )
        {
            Button b =
                    new NativeButton( view.substring( 0, 1 ).toUpperCase() + view.substring( 1 ).replace( '-', ' ' ) );
            b.setId( view.substring( 0, 1 ).toUpperCase() + view.substring( 1 ).replace( '-', ' ' ) );
            b.addStyleName( "icon-" + view );
            b.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    clearMenuSelection();
                    event.getButton().addStyleName( "selected" );
                    if ( !nav.getState().equals( "/" + view ) )
                    {
                        nav.navigateTo( "/" + view );
                    }
                }
            } );

            menu.addComponent( b );
            viewNameToMenuButton.put( "/" + view, b );
        }
        menu.addStyleName( "menu" );
        menu.setHeight( "100%" );

        String f = Page.getCurrent().getUriFragment();
        if ( f != null && f.startsWith( "!" ) )
        {
            f = f.substring( 1 );
        }
        if ( f == null || f.equals( "" ) || f.equals( "/" ) )
        {
            nav.navigateTo( "/modules" );
            menu.getComponent( 0 ).addStyleName( "selected" );
            helpManager.showHelpFor( ModulesView.class );
        }
        else
        {
            nav.navigateTo( f );
            helpManager.showHelpFor( routes.get( f ) );
            viewNameToMenuButton.get( f ).addStyleName( "selected" );
        }

        nav.addViewChangeListener( new ViewChangeListener()
        {

            @Override
            public boolean beforeViewChange( ViewChangeListener.ViewChangeEvent event )
            {
                helpManager.closeAll();
                return true;
            }


            @Override
            public void afterViewChange( ViewChangeListener.ViewChangeEvent event )
            {
                View newView = event.getNewView();
                helpManager.showHelpFor( newView );
            }
        } );
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


    public CoreModulesView getCoreModulesView()
    {
        return coreModulesView;
    }


    public ModulesView getModulesView()
    {
        return modulesView;
    }
}
