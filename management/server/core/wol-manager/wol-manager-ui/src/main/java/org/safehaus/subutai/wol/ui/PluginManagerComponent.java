package org.safehaus.subutai.wol.ui;


import java.net.MalformedURLException;
import java.net.URL;

import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.wol.api.PluginInfo;
import org.safehaus.subutai.wol.api.PluginManager;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;


/**
 * Created by ebru on 08.12.2014.
 */
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
    //Reindeer.TABLE_STRONG
    private GridLayout contentRoot;
    private Table pluginsTable;
    private PluginManagerPortalModule managerUI;
    private PluginManager pluginManager;


    public PluginManagerComponent( PluginManagerPortalModule managerUI, PluginManager pluginManager )
    {
        this.managerUI = managerUI;
        this.pluginManager = pluginManager;

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

        /*URL url = null;
        try
        {
            url = new URL("http://dev.vaadin.com/");
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
        }
        Embedded browser = new Embedded("", new ExternalResource(url));
        browser.setType(Embedded.TYPE_BROWSER);
        controlsContent.addComponent(browser);*/

        getListInstalledPluginsButton( controlsContent );
        getListAvailablePluginsButton( controlsContent );


        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( pluginsTable, 0, 1, 0, 9 );

        setCompositionRoot( contentRoot );
    }
    private void addTableToContent()
    {
        contentRoot.addComponent( pluginsTable, 0, 1, 0, 9 );
    }


    private void getListInstalledPluginsButton( HorizontalLayout controlsContent )
    {
        Button listPluginsBtn = new Button( LIST_INSTALLED_PLUGINS_BUTTON_CAPTION );
        listPluginsBtn.setId( "listInstalledPluginsBtn" );
        listPluginsBtn.addStyleName( STYLE_NAME );
        listPluginsBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                listInstalledPluginsClickHandler();
            }
        } );

        controlsContent.addComponent( listPluginsBtn );
    }

    private void getListAvailablePluginsButton( HorizontalLayout controlsContent )
    {
        Button listAvailablePluginsBtn = new Button( LIST_AVAILABLE_PLUGINS_BUTTON_CAPTION );
        listAvailablePluginsBtn.setId( "listAvailablePluginsBtn" );
        listAvailablePluginsBtn.addStyleName( STYLE_NAME );
        listAvailablePluginsBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                listInstalledPluginsClickHandler();
            }
        }  );

        controlsContent.addComponent( listAvailablePluginsBtn );
    }

    private void getMarketPlaceButton( final HorizontalLayout controlsContent)
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
        URL url = null;
        try
        {
            url = new URL("http://dev.vaadin.com/");
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
        }
        Embedded browser = new Embedded("", new ExternalResource(url));
        controlsContent.addComponent(browser);
    }


    private void listInstalledPluginsClickHandler()
    {
        pluginsTable.removeAllItems();
        boolean isUpgradeAvailable = false;

        for ( PluginInfo p : pluginManager.getInstalledPlugins() )
        {
            final Label version = new Label();
            version.setValue( p.getPackageVersion() );
            final Button upgradeButton = new Button( UPGRADE_BUTTON_CAPTION );
            final Button removeButton = new Button (REMOVE_BUTTON_CAPTION );
            isUpgradeAvailable = pluginManager.isUpgradeAvailable( p.getPluginName() );
            if( isUpgradeAvailable )
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
        }
    }

    private void listAvailablePluginsClickHandler()
    {
        pluginsTable.removeAllItems();

        for( PluginInfo p : pluginManager.getAvailablePlugins() )
        {
            final Label version = new Label();
            version.setValue( p.getPackageVersion() );
            final Button installButton = new Button( INSTALL_BUTTON_CAPTION );
            final HorizontalLayout availableOperations = new HorizontalLayout();
            addStyleName( installButton, availableOperations );
            addGivenComponents( availableOperations, installButton );

            pluginsTable.addItem( new Object[] {
                    p.getPluginName(), version, availableOperations
            }, null );
        }
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
        this.pluginManager = null;
    }
}
