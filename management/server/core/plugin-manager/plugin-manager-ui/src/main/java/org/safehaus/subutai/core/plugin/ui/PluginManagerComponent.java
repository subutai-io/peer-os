package org.safehaus.subutai.core.plugin.ui;


import org.safehaus.subutai.common.protocol.Disposable;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
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


    public PluginManagerComponent( PluginManagerPortalModule managerUI )
    {
        this.managerUI = managerUI;

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

        getListPluginsButton();

    }


    private void getListPluginsButton()
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
    }


    private void refreshPluginsInfo()
    {

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

    }
}
