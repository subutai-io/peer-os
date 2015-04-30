package org.safehaus.subutai.core.env.ui.forms;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.common.environment.EnvironmentStatus;
import org.safehaus.subutai.common.mdc.SubutaiExecutors;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class ContainersWindow extends Window
{
    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;
    private Environment environment;
    private Table containersTable;
    private ScheduledExecutorService updater;
    private ExecutorService taskExecutor;


    public ContainersWindow( final EnvironmentManager environmentManager, final Environment environment,
                             final PeerManager peerManager )
    {
        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
        this.environment = environment;

        setCaption( "Containers" );
        setWidth( "900px" );
        setHeight( "600px" );
        setModal( true );
        setClosable( true );

        VerticalLayout content = new VerticalLayout();
        content.setSpacing( true );
        content.setMargin( true );
        content.setStyleName( "default" );
        content.setSizeFull();

        containersTable = createContainersTable();

        updateContainersTable();

        content.addComponent( containersTable );

        setContent( content );

        addDetachListener( new DetachListener()
        {
            @Override
            public void detach( final DetachEvent event )
            {
                updater.shutdown();
                taskExecutor.shutdown();
            }
        } );
        addAttachListener( new AttachListener()
        {
            @Override
            public void attach( final AttachEvent event )
            {
                startTableUpdateThread();
            }
        } );
    }


    private void startTableUpdateThread()
    {
        taskExecutor = SubutaiExecutors.newSingleThreadExecutor();
        updater = SubutaiExecutors.newSingleThreadScheduledExecutor();
        updater.scheduleWithFixedDelay( new Runnable()
        {
            @Override
            public void run()
            {

                containersTable.getUI().access( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateContainersTable();
                    }
                } );
            }
        }, 3, 30, TimeUnit.SECONDS );
    }


    private void updateContainersTable()
    {
        containersTable.removeAllItems();
        for ( final ContainerHost containerHost : environment.getContainerHosts() )
        {
            final Button startBtn = new Button( "Start" );
            final Button tagsBtn = new Button( "Tags" );
            final Button stopBtn = new Button( "Stop" );
            final Button destroyBtn = new Button( "Destroy" );
            tagsBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent event )
                {
                    getUI().addWindow( new TagsWindow( containerHost, peerManager.getLocalPeer() ) );
                }
            } );
            final Runnable startRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        containerHost.start();
                    }
                    catch ( PeerException e )
                    {
                        String msg = String.format( "Error starting container %s: %s", containerHost.getHostname(), e );
                        Notification.show( msg, Notification.Type.ERROR_MESSAGE );
                    }
                    finally
                    {
                        enableTable();
                    }
                }
            };
            startBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent event )
                {
                    disableTable();
                    taskExecutor.submit( startRunnable );
                }
            } );
            final Runnable stopRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        containerHost.stop();
                    }
                    catch ( PeerException e )
                    {
                        String msg = String.format( "Error stopping container %s: %s", containerHost.getHostname(), e );
                        Notification.show( msg, Notification.Type.ERROR_MESSAGE );
                    }
                    finally
                    {
                        enableTable();
                    }
                }
            };
            stopBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent event )
                {
                    disableTable();
                    taskExecutor.submit( stopRunnable );
                }
            } );
            destroyBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent event )
                {
                    destroyOperation( containerHost );
                }
            } );
            containersTable.addItem( new Object[] {
                    containerHost.getId().toString(), containerHost.getTemplateName(), containerHost.getHostname(),
                    containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ), tagsBtn, startBtn,
                    stopBtn, destroyBtn
            }, null );
            boolean isContainerConnected = containerHost.isConnected();
            startBtn.setEnabled( !isContainerConnected );
            stopBtn.setEnabled( isContainerConnected );
        }

        containersTable.refreshRowCache();
    }


    private void destroyOperation( final ContainerHost containerHost )
    {
        ConfirmationDialog alert =
                new ConfirmationDialog( "Do you really want to destroy this container?", "Yes", "No" );
        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    containerHost.dispose();
                    try
                    {
                        environment = environmentManager.findEnvironment( environment.getId() );
                    }
                    catch ( EnvironmentNotFoundException e )
                    {
                        close();
                    }
                }
                catch ( PeerException e )
                {
                    String msg = String.format( "Error destroying container %s: %s", containerHost.getHostname(), e );
                    Notification.show( msg, Notification.Type.ERROR_MESSAGE );
                }
                finally
                {
                    enableTable();
                }
            }
        };
        alert.getOk().addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                disableTable();
                taskExecutor.submit( runnable );
            }
        } );
        getUI().addWindow( alert.getAlert() );
    }


    private void enableTable()
    {
        if ( environment.getStatus() != EnvironmentStatus.UNDER_MODIFICATION )
        {
            updateContainersTable();
            containersTable.setEnabled( true );
        }
    }


    private void disableTable()
    {
        containersTable.setEnabled( false );
        Notification.show( "Please, wait..." );
    }


    private Table createContainersTable()
    {
        Table table = new Table();
        table.addContainerProperty( "Id", String.class, null );
        table.addContainerProperty( "Template", String.class, null );
        table.addContainerProperty( "Hostname", String.class, null );
        table.addContainerProperty( "IP", String.class, null );
        table.addContainerProperty( "Tags", Button.class, null );
        table.addContainerProperty( "Start", Button.class, null );
        table.addContainerProperty( "Stop", Button.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }
}
