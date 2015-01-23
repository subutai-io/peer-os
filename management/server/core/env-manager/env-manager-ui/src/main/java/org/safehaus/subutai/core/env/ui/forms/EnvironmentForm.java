package org.safehaus.subutai.core.env.ui.forms;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.env.api.Environment;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentNotFoundException;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
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
    private ExecutorService executorService = Executors.newCachedThreadPool();


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


    public void updateEnvironmentsTable()
    {
        environmentsTable.removeAllItems();
        for ( final Environment environment : environmentManager.getEnvironments() )
        {
            final Button destroy = new Button( DESTROY );
            destroy.setId( environment.getName() + "-destroy" );
            destroy.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    destroyEnvironment( environment );
                }
            } );
            environmentsTable.addItem( new Object[] {
                    environment.getName(), environment.getStatus().name(), destroy
            }, null );
        }
        environmentsTable.refreshRowCache();
    }


    private void destroyEnvironment( final Environment environment )
    {
        executorService.execute( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    environmentManager.destroyEnvironment( environment.getId() );
                }
                catch ( EnvironmentNotFoundException | EnvironmentDestructionException e )
                {
                    Notification
                            .show( "Error destroying environment", e.getMessage(), Notification.Type.ERROR_MESSAGE );
                }

                updateEnvironmentsTable();
            }
        } );

        Notification.show( "Environment destruction started" );
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
