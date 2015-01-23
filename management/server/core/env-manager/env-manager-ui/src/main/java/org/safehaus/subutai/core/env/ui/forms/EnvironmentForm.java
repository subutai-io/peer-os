package org.safehaus.subutai.core.env.ui.forms;


import org.safehaus.subutai.core.env.api.Environment;
import org.safehaus.subutai.core.env.api.EnvironmentManager;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


public class EnvironmentForm
{
    private final EnvironmentManager environmentManager;
    private static final String NAME = "Name";
    private static final String STATUS = "Status";
    private static final String DESTROY = "Destroy";
    private static final String VIEW_ENVIRONMENTS = "View environments";

    private final VerticalLayout contentRoot;
    private Table environmentsTable;


    public EnvironmentForm( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;

        contentRoot = new VerticalLayout();

        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        Button viewEnvironmentsButton = new Button( VIEW_ENVIRONMENTS );
        viewEnvironmentsButton.setId( "viewEnvironmentsButton" );

        viewEnvironmentsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                updateEnvironmentsTable();
            }
        } );

        environmentsTable = createEnvironmentsTable( "Environments" );
        environmentsTable.setId( "Environments" );

        contentRoot.addComponent( viewEnvironmentsButton );
        contentRoot.addComponent( environmentsTable );
    }


    private void updateEnvironmentsTable()
    {
        for ( Environment environment : environmentManager.getEnvironments() )
        {
            environmentsTable.addItem( new Object[] {
                    environment.getName(), environment.getStatus().name(), null
            }, null );
        }
    }


    private Table createEnvironmentsTable( String caption )
    {
        Table table = new Table( caption );
        table.addContainerProperty( NAME, String.class, null );
        table.addContainerProperty( STATUS, String.class, null );
        table.addContainerProperty( DESTROY, Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
