package org.safehaus.subutai.core.plugin.ui;


import java.awt.Label;

import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.plugin.api.PluginInfo;
import org.safehaus.subutai.core.plugin.api.PluginManager;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;


/**
 * Created by ebru on 08.12.2014.
 */
public class PluginManagerComponent extends CustomComponent implements Disposable
{
    protected static final String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    protected static final String LIST_PLUGINS_CAPTION = "List Plugins";
    protected static final String UPGRADE_CAPTION = "Upgrade";
    protected static final String PLUGIN_COLUMN_CAPTION = "Plugin";
    protected static final String VERSION_COLUMN_CAPTION = "Version";
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

        contentRoot = new GridLayout( );
        contentRoot.setColumns( 1 );
        contentRoot.setRows( 20 );
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();

        pluginsTable = createTableTemplate( "Plugins" );
        pluginsTable.setId( "PluginsTable" );

        HorizontalLayout controlsContent = new HorizontalLayout( );
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

        getListPluginsButton( controlsContent);

        contentRoot.addComponent( controlsContent,0,0 );
        contentRoot.addComponent( pluginsTable, 0, 1, 0, 9 );

    }


    private void getListPluginsButton(HorizontalLayout controlsContent)
    {
        Button listPluginsBtn = new Button( LIST_PLUGINS_CAPTION );
        listPluginsBtn.setId( "listPluginsBtn" );
        listPluginsBtn.addStyleName( "default" );
        listPluginsBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                refreshPluginsInfo();
            }
        } );

        controlsContent.addComponent( listPluginsBtn );
    }


    private void refreshPluginsInfo()
    {
        pluginsTable.removeAllItems();

        for( PluginInfo p : pluginManager.getInstalledPlugins() )
        {
            final Label version = new Label();
            version.setText( p.getPackageVersion() );
            final Button upgradeButton = new Button( UPGRADE_CAPTION );
            final HorizontalLayout availableOperations = new HorizontalLayout();
            addStyleName( upgradeButton, availableOperations );
            addGivenComponents( availableOperations, upgradeButton );

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



    private Table createTableTemplate( String caption)
    {
        final Table table = new Table( caption );
        table.setStyleName( "Reindeer.TABLE_STRONG" );
        table.addContainerProperty( PLUGIN_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( VERSION_COLUMN_CAPTION, String.class, null );
        table.addContainerProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION, String.class, null );
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
