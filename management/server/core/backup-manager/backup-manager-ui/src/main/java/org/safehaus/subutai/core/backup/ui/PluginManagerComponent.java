package org.safehaus.subutai.core.backup.ui;


import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;

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


public class PluginManagerComponent extends CustomComponent implements Disposable
{
    protected static final String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected static final String MARKET_PLACE_BUTTON_CAPTION = "Market Place";
    protected static final String LIST_INSTALLED_PLUGINS_BUTTON_CAPTION = "List Installed Plugins";
    protected static final String LIST_AVAILABLE_PLUGINS_BUTTON_CAPTION = "List Available Plugins";
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
    private final ExecutorService executorService;
    private Tracker tracker;
    private boolean isTableRemoved = false;


    public PluginManagerComponent( final ExecutorService executorService, PluginManagerPortalModule managerUI,
                                   Tracker tracker )
    {
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

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        getListInstalledPluginsButton( controlsContent );
        getListAvailablePluginsButton( controlsContent );
        getMarketPlaceButton( controlsContent );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( pluginsTable, 0, 1, 0, 9 );

        setCompositionRoot( contentRoot );
    }


    private void addTableToContent()
    {
        contentRoot.addComponent( pluginsTable, 0, 1, 0, 9 );
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
    }


    private void addClickListenerToInstallButton( Button installButton, final String pluginName, final Object itemId )
    {
        installButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                ConfirmationDialog alert =
                        new ConfirmationDialog( String.format( "Do you want to remove the %s plugin?", pluginName ),
                                "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {


                    }
                } );

                contentRoot.getUI().addWindow( alert.getAlert() );
            }
        } );
    }


    private void addClickListenetToUpgradeButton( final Button upgradeButton, final String pluginName )
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


                    }
                } );

                contentRoot.getUI().addWindow( alert.getAlert() );
            }
        } );
    }


    private void addClickListenerToRemoveButton( Button removeButton, final String pluginName, final Object itemId )
    {
        removeButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                ConfirmationDialog alert =
                        new ConfirmationDialog( String.format( "Do you want to remove the %s plugin?", pluginName ),
                                "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {


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
        table.addContainerProperty( VERSION_COLUMN_CAPTION, Label.class, null );
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, HorizontalLayout.class, null );
        table.setSizeFull();
        table.setPageLength( 20 );
        table.setSelectable( false );
        table.setImmediate( true );
        return table;
    }


    @Override
    public void dispose()
    {

    }
}
