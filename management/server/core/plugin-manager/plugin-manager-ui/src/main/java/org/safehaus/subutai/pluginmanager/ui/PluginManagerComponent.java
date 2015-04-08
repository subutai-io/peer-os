package org.safehaus.subutai.pluginmanager.ui;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.pluginmanager.api.OperationType;
import org.safehaus.subutai.pluginmanager.api.PluginInfo;
import org.safehaus.subutai.pluginmanager.api.PluginManager;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


public class PluginManagerComponent extends CustomComponent
{
    protected static final String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected static final String MARKET_PLACE_BUTTON_CAPTION = "Market Place";
    protected static final String LIST_INSTALLED_PLUGINS_BUTTON_CAPTION = "List Installed Plugins";
    protected static final String LIST_AVAILABLE_PLUGINS_BUTTON_CAPTION = "List Available Plugins";
    protected static final String LIST_PLUGINS_BUTTON_CAPTION = "List Plugins";
    protected static final String SEARCH_BUTTON_CAPTION = "Search";
    protected static final String INSTALL_BUTTON_CAPTION = "Install";
    protected static final String REMOVE_BUTTON_CAPTION = "Remove";
    protected static final String UPGRADE_BUTTON_CAPTION = "Upgrade";
    protected static final String PLUGIN_COLUMN_CAPTION = "Plugin";
    protected static final String VERSION_COLUMN_CAPTION = "Version";
    protected static final String RATE_COLUMN_CAPTION = "Rate";
    protected static final String STYLE_NAME = "default";
    private GridLayout contentRoot;
    private Table pluginsTable;
    private BrowserFrame browser;
    private PluginManager pluginManager;
    private final ExecutorService executorService;
    private Tracker tracker;
    private boolean isTableRemoved = false;
    private ScheduledExecutorService updater;


    public PluginManagerComponent( final ExecutorService executorService, PluginManagerPortalModule managerUI,
                                   PluginManager pluginManager, Tracker tracker )
    {
        this.pluginManager = pluginManager;
        this.executorService = executorService;
        this.tracker = tracker;

        contentRoot = new GridLayout();
        contentRoot.setColumns( 1 );
        contentRoot.setRows( 20 );
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();

        pluginsTable = createTableTemplate( "Plugins" );
        pluginsTable.setId( "PluginsTable" );

        final HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        getListPluginsButton( controlsContent );
        //getListInstalledPluginsButton( controlsContent );
        //getListAvailablePluginsButton( controlsContent );
        //getMarketPlaceButton( controlsContent );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( pluginsTable, 0, 1, 0, 9 );

        setCompositionRoot( contentRoot );

        addDetachListener( new DetachListener()
        {
            @Override
            public void detach( final DetachEvent event )
            {
                if ( updater != null )
                {
                    updater.shutdown();
                }
            }
        } );
    }


    private void startTableUpdateThread( final HorizontalLayout controlsContent )
    {
        if ( updater != null )
        {
            updater.shutdown();
        }
        updater = Executors.newSingleThreadScheduledExecutor();
        updater.scheduleWithFixedDelay( new Runnable()
        {
            @Override
            public void run()
            {

                pluginsTable.getUI().access( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listPluginsClickHandler();
                    }
                } );
            }
        }, 3, 5, TimeUnit.SECONDS );
    }


    private void addTableToContent()
    {
        contentRoot.addComponent( pluginsTable, 0, 1, 0, 9 );
    }


    private void getListPluginsButton( final HorizontalLayout controlsContent )
    {
        Button listPluginsBtn = new Button( LIST_PLUGINS_BUTTON_CAPTION );
        listPluginsBtn.setId( "listPluginsBtn" );
        listPluginsBtn.addStyleName( STYLE_NAME );
        listPluginsBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                listPluginsClickHandler();
            }
        } );

        controlsContent.addComponent( listPluginsBtn );
    }


    private void getListInstalledPluginsButton( final HorizontalLayout controlsContent )
    {
        Button listPluginsBtn = new Button( LIST_INSTALLED_PLUGINS_BUTTON_CAPTION );
        listPluginsBtn.setId( "listInstalledPluginsBtn" );
        listPluginsBtn.addStyleName( STYLE_NAME );
        listPluginsBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                listInstalledPluginsClickHandler( controlsContent );
            }
        } );

        controlsContent.addComponent( listPluginsBtn );
    }


    private void getListAvailablePluginsButton( final HorizontalLayout controlsContent )
    {
        Button listAvailablePluginsBtn = new Button( LIST_AVAILABLE_PLUGINS_BUTTON_CAPTION );
        listAvailablePluginsBtn.setId( "listAvailablePluginsBtn" );
        listAvailablePluginsBtn.addStyleName( STYLE_NAME );
        listAvailablePluginsBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                listAvailablePluginsClickHandler( controlsContent );
            }
        } );

        controlsContent.addComponent( listAvailablePluginsBtn );
    }


    private void getMarketPlaceButton( final HorizontalLayout controlsContent )
    {
        Button marketPlaceBtn = new Button( MARKET_PLACE_BUTTON_CAPTION );
        marketPlaceBtn.setId( "marketPlaceBtn" );
        marketPlaceBtn.addStyleName( STYLE_NAME );
        marketPlaceBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                marketPlaceButtonClickListener( controlsContent );
            }
        } );
        controlsContent.addComponent( marketPlaceBtn );
    }


    private void marketPlaceButtonClickListener( HorizontalLayout controlsContent )
    {
        contentRoot.removeComponent( pluginsTable );
        isTableRemoved = true;
        //TODO go HUB marketplace page
        browser = new BrowserFrame( "vaadin.com", new ExternalResource( "https://vaadin.com/home" ) );
        browser.setWidth( "800px" );
        browser.setHeight( "800px" );
        controlsContent.addComponent( browser );
    }


    private void refreshUI()
    {
        listPluginsClickHandler();
    }


    private void listInstalledPluginsClickHandler( HorizontalLayout controlsContent )
    {
        if ( browser != null )
        {
            controlsContent.removeComponent( browser );
        }
        pluginsTable.removeAllItems();
        if ( isTableRemoved )
        {
            contentRoot.addComponent( pluginsTable );
        }
        boolean isUpgradeAvailable = false;

        for ( PluginInfo p : pluginManager.getInstalledPlugins() )
        {
            final Label version = new Label();
            version.setValue( p.getVersion() );
            final Button upgradeButton = new Button( UPGRADE_BUTTON_CAPTION );
            final Button removeButton = new Button( REMOVE_BUTTON_CAPTION );
            isUpgradeAvailable = pluginManager.isUpgradeAvailable( p.getPluginName() );
            if ( isUpgradeAvailable )
            {
                upgradeButton.setVisible( true );
            }
            else
            {
                upgradeButton.setVisible( false );
            }
            final HorizontalLayout availableOperations = new HorizontalLayout();
            addStyleName( upgradeButton, removeButton, availableOperations );
            addGivenComponents( availableOperations, upgradeButton, removeButton );

            pluginsTable.addItem( new Object[] {
                    p.getPluginName(), version, availableOperations
            }, null );
            List<Object> itemIds = new ArrayList<>( pluginsTable.getItemIds() );
            int count = itemIds.size();
            Object itemId = itemIds.get( count - 1 );

            //addClickListenerToRemoveButton( removeButton, p.getPluginName(), itemId, availableOperations );
            //addClickListenetToUpgradeButton( upgradeButton, p.getPluginName() );
            isTableRemoved = false;
        }
    }


    private void listPluginsClickHandler()
    {
        pluginsTable.removeAllItems();
        boolean isInstalled = false;
        for ( String p : pluginManager.getAvailablePluginNames() )
        {
            final Button installButton = new Button( INSTALL_BUTTON_CAPTION );
            final Button removeButton = new Button( REMOVE_BUTTON_CAPTION );
            isInstalled = pluginManager.isInstalled( p );
            final HorizontalLayout availableOperations = new HorizontalLayout();
            addStyleName( installButton, removeButton, availableOperations );

            if ( isInstalled )
            {
                addGivenComponents( availableOperations, removeButton );
            }
            else
            {
                addGivenComponents( availableOperations, installButton );
            }


            pluginsTable.addItem( new Object[] { p, availableOperations }, null );

            List<Object> itemIds = new ArrayList<>( pluginsTable.getItemIds() );
            if ( itemIds.size() == 0 )
            {
                com.vaadin.ui.Notification.show( "There is no available plugins in repo to be installed" );
                return;
            }
            else
            {
                int count = itemIds.size();
                Object itemId = itemIds.get( count - 1 );
                addClickListenerToRemoveButton( removeButton, p, itemId, availableOperations, installButton );
                addClickListenerToInstallButton( installButton, p, itemId, availableOperations, removeButton );
            }
        }
    }


    private void listAvailablePluginsClickHandler( HorizontalLayout controlsContent )
    {
        if ( browser != null )
        {
            controlsContent.removeComponent( browser );
        }


        pluginsTable.removeAllItems();
        if ( isTableRemoved )
        {
            contentRoot.addComponent( pluginsTable );
        }


        for ( PluginInfo p : pluginManager.getAvailablePlugins() )
        {
            final Label version = new Label();
            version.setValue( p.getVersion() );
            final Button installButton = new Button( INSTALL_BUTTON_CAPTION );
            final HorizontalLayout availableOperations = new HorizontalLayout();
            addStyleName( installButton, availableOperations );
            addGivenComponents( availableOperations, installButton );

            pluginsTable.addItem( new Object[] {
                    p.getPluginName(), version, availableOperations
            }, null );

            List<Object> itemIds = new ArrayList<>( pluginsTable.getItemIds() );
            if ( itemIds.size() == 0 )
            {
                com.vaadin.ui.Notification.show( "There is no available plugins in repo to be installed" );
                return;
            }
            else
            {
                int count = itemIds.size();
                Object itemId = itemIds.get( count - 1 );
                //addClickListenerToInstallButton( installButton, p.getPluginName(), itemId );
            }


            isTableRemoved = false;
        }
    }


    private void addClickListenerToInstallButton( final Button installButton, final String pluginName,
                                                  final Object itemId, final HorizontalLayout availableOperations,
                                                  final Button removeButton )
    {
        installButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                pluginsTable.setEnabled( false );

                ConfirmationDialog alert =
                        new ConfirmationDialog( String.format( "Do you want to install the %s plugin?", pluginName ),
                                "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {

                        UUID trackID = pluginManager.installPlugin( pluginName );

                        ProgressWindow window =
                                new ProgressWindow( executorService, tracker, trackID, pluginManager.getProductKey() );
                        window.getWindow().addCloseListener( new Window.CloseListener()
                        {
                            @Override
                            public void windowClose( Window.CloseEvent closeEvent )
                            {
                                if ( pluginManager.operationSuccessful( OperationType.INSTALL ) )
                                {
                                    availableOperations.removeComponent( installButton );
                                    availableOperations.addComponent( removeButton );
                                }
                                pluginsTable.setEnabled( true );
                            }
                        } );
                        contentRoot.getUI().addWindow( window.getWindow() );
                    }
                } );
                alert.getCancel().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        pluginsTable.setEnabled( true );
                    }
                } );
                contentRoot.getUI().addWindow( alert.getAlert() );
            }
        } );
    }


    private void addClickListenerToUpgradeButton( final Button upgradeButton, final String pluginName )
    {
        upgradeButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                ConfirmationDialog alert =
                        new ConfirmationDialog( String.format( "Do you want to upgrade the %s plugin?", pluginName ),
                                "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {

                        UUID trackID = pluginManager.upgradePlugin( pluginName );

                        ProgressWindow window =
                                new ProgressWindow( executorService, tracker, trackID, pluginManager.getProductKey() );
                        window.getWindow().addCloseListener( new Window.CloseListener()
                        {
                            @Override
                            public void windowClose( Window.CloseEvent closeEvent )
                            {
                                upgradeButton.setVisible( false );
                            }
                        } );
                        contentRoot.getUI().addWindow( window.getWindow() );
                    }
                } );

                contentRoot.getUI().addWindow( alert.getAlert() );
            }
        } );
    }


    private void addClickListenerToRemoveButton( final Button removeButton, final String pluginName,
                                                 final Object itemId, final HorizontalLayout availableOperations,
                                                 final Button installButton )
    {
        removeButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                pluginsTable.setEnabled( false );

                ConfirmationDialog alert =
                        new ConfirmationDialog( String.format( "Do you want to remove the %s plugin?", pluginName ),
                                "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {

                        UUID trackID = pluginManager.removePlugin( pluginName );

                        ProgressWindow window =
                                new ProgressWindow( executorService, tracker, trackID, pluginManager.getProductKey() );
                        window.getWindow().addCloseListener( new Window.CloseListener()
                        {
                            @Override
                            public void windowClose( Window.CloseEvent closeEvent )
                            {
                                if ( pluginManager.operationSuccessful( OperationType.REMOVE ) )
                                {
                                    availableOperations.removeComponent( removeButton );
                                    availableOperations.addComponent( installButton );
                                }
                                pluginsTable.setEnabled( true );
                            }
                        } );
                        contentRoot.getUI().addWindow( window.getWindow() );
                    }
                } );
                alert.getCancel().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        pluginsTable.setEnabled( true );
                    }
                } );
                contentRoot.getUI().addWindow( alert.getAlert() );
            }
        } );
    }


    private void addStyleName( Component... components )
    {
        for ( Component c : components )
        {
            c.addStyleName( STYLE_NAME );
        }
    }


    private void addGivenComponents( Layout layout, Button... buttons )
    {
        for ( Button b : buttons )
        {
            layout.addComponent( b );
        }
    }


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.setStyleName( "Reindeer.TABLE_STRONG" );
        table.addContainerProperty( PLUGIN_COLUMN_CAPTION, String.class, null );
        //table.addContainerProperty( VERSION_COLUMN_CAPTION, Label.class, null );
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, HorizontalLayout.class, null );
        table.setSizeFull();
        table.setPageLength( 20 );
        table.setSelectable( false );
        table.setImmediate( true );
        return table;
    }


    //    @Override
    //    public void dispose()
    //    {
    //        this.pluginManager = null;
    //    }
}
