package org.safehaus.subutai.core.env.ui.forms;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.core.env.api.Environment;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.api.EnvironmentStatus;
import org.safehaus.subutai.core.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentNotFoundException;

import com.vaadin.server.ClientConnector;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


public class EnvironmentForm
{
    private static final String SSH_KEY = "Ssh key";
    private static final String DATE = "Date";
    private final EnvironmentManager environmentManager;
    private static final String CONTAINERS = "Containers";
    private static final String NAME = "Name";
    private static final String STATUS = "Status";
    private static final String DESTROY = "Destroy";
    private static final String VIEW_ENVIRONMENTS = "View environments";
    private static final String OK_ICON_SOURCE = "img/ok.png";
    private static final String ERROR_ICON_SOURCE = "img/cancel.png";
    private static final String LOAD_ICON_SOURCE = "img/spinner.gif";

    private final VerticalLayout contentRoot;
    private Table environmentsTable;
    private ScheduledExecutorService updater = Executors.newSingleThreadScheduledExecutor();


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

        //        contentRoot.addComponent( viewEnvironmentsButton );
        contentRoot.addComponent( environmentsTable );

        contentRoot.addDetachListener( new ClientConnector.DetachListener()
        {
            @Override
            public void detach( final ClientConnector.DetachEvent event )
            {
                updater.shutdown();
            }
        } );

        startTableUpdateThread();
    }


    private void startTableUpdateThread()
    {
        updater.scheduleWithFixedDelay( new Runnable()
        {
            @Override
            public void run()
            {

                environmentsTable.getUI().access( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateEnvironmentsTable();
                    }
                } );
            }
        }, 3, 5, TimeUnit.SECONDS );
    }


    private void updateEnvironmentsTable()
    {
        environmentsTable.removeAllItems();
        for ( final Environment environment : environmentManager.getEnvironments() )
        {
            final Button containersBtn = new Button( CONTAINERS );
            final Button sshKeyBtn = new Button( SSH_KEY );
            final Button destroyBtn = new Button( DESTROY );
            containersBtn.setId( environment.getName() + "-containers" );
            containersBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent event )
                {
                    contentRoot.getUI().addWindow( new ContainersWindow( environmentManager, environment ) );
                }
            } );

            destroyBtn.setId( environment.getName() + "-destroy" );
            destroyBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    destroyBtn.setEnabled( false );
                    containersBtn.setEnabled( false );
                    sshKeyBtn.setEnabled( false );
                    destroyEnvironment( environment );
                }
            } );

            sshKeyBtn.setId( environment.getName() + "-sshkey" );
            sshKeyBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent event )
                {
                    contentRoot.getUI().addWindow( new SshKeyWindow( environment ) );
                }
            } );

            boolean isEnvironmentUnderModification =
                    environment.getStatus().equals( EnvironmentStatus.UNDER_MODIFICATION );

            destroyBtn.setEnabled( !isEnvironmentUnderModification );
            containersBtn.setEnabled( !isEnvironmentUnderModification );
            sshKeyBtn.setEnabled( !isEnvironmentUnderModification );

            Embedded icon = isEnvironmentUnderModification ? new Embedded( "", new ThemeResource( LOAD_ICON_SOURCE ) ) :
                            environment.getStatus().equals( EnvironmentStatus.HEALTHY ) ?
                            new Embedded( "", new ThemeResource( OK_ICON_SOURCE ) ) :
                            new Embedded( "", new ThemeResource( ERROR_ICON_SOURCE ) );


            environmentsTable.addItem( new Object[] {
                    environment.getName(), getCreationDate( environment.getCreationTimestamp() ), icon, containersBtn,
                    sshKeyBtn, destroyBtn
            }, null );
        }
        environmentsTable.refreshRowCache();
    }


    private String getCreationDate( long ts )
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "MMM dd,yyyy HH:mm" );
        Date date = new Date( ts );
        return sdf.format( date );
    }


    private void destroyEnvironment( final Environment environment )
    {
        try
        {
            environmentManager.destroyEnvironment( environment.getId(), true, false );

            Notification.show( "Environment destruction started" );
        }
        catch ( EnvironmentDestructionException | EnvironmentNotFoundException e )
        {
            Notification.show( "Error destroying environment", e.getMessage(), Notification.Type.ERROR_MESSAGE );
        }
    }


    private Table createEnvironmentsTable( String caption )
    {
        Table table = new Table( caption );
        table.addContainerProperty( NAME, String.class, null );
        table.addContainerProperty( DATE, String.class, null );
        table.addContainerProperty( STATUS, Embedded.class, null );
        table.addContainerProperty( CONTAINERS, Button.class, null );
        table.addContainerProperty( SSH_KEY, Button.class, null );
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
