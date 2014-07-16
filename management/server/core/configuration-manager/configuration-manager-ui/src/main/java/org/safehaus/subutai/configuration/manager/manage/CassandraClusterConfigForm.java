package org.safehaus.subutai.configuration.manager.manage;


import org.safehaus.subutai.configuration.manager.api.ConfigManager;

import org.apache.cassandra.config.Config;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class CassandraClusterConfigForm {

    private VerticalLayout contentRoot;
    private Table table;
    private ConfigManager configManager;
    private Config config;


    public CassandraClusterConfigForm( final ConfigManager configManager ) {
        this.configManager = configManager;


        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        table = createTable( "Blueprints", 300 );

        Button getEnvironmentsButton = new Button( "View" );

        getEnvironmentsButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                //                updateTableData();
//                config = configManager.getCassandraConfig();
//                fillConfigForm( config );
            }
        } );

        contentRoot.addComponent( getEnvironmentsButton );
        contentRoot.addComponent( table );
    }


    public VerticalLayout getContentRoot() {
        return this.contentRoot;
    }


    private Table createTable( String caption, int size ) {
        Table table = new Table( caption );
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "View", Button.class, null );
        table.addContainerProperty( "Build environment", Button.class, null );
        table.addContainerProperty( "Delete", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }

    private void fillConfigForm(Config config) {

        table.addItem( new Object[] {config.commitlog_directory,
        new Button("Set"), new Button("Run"), new Button("Delete")});

    }
}
