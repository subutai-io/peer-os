package io.subutai.core.peer.ui.container.manage;


import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.peer.ui.container.common.Buttons;
import io.subutai.server.ui.component.ConfirmationDialog;


@SuppressWarnings( "serial" )
public class Manager extends VerticalLayout
{

    private static final String HOST_NAME = "Host name";
    private static final String LXC_STATUS = "Status";


    private final Label indicator;
    private final TreeTable lxcTable;
    private final PeerManager peerManager;
    private ExecutorService executorService;

    private static final AtomicInteger PENDING_PROCESSES = new AtomicInteger( 0 );

    private static final Action TAG_CONTAINER = new Action( "Tags" );
    private static final Action START_CONTAINER = new Action( "Start" );
    private static final Action STOP_CONTAINER = new Action( "Stop" );
    private static final Action DESTROY_CONTAINER = new Action( "Destroy" );

    private static final Action START_ALL = new Action( "Start All" );
    private static final Action STOP_ALL = new Action( "Stop All" );
    private static final Action DESTROY_ALL = new Action( "Destroy All" );

    private static final Logger LOG = LoggerFactory.getLogger( Manager.class );

    private static final String DEFAULT_STYLE_NAME = "default";

    private Action.Handler contextMenu;


    public Manager( final PeerManager peerManager )
    {


        setSpacing( true );
        setMargin( true );

        this.peerManager = peerManager;

        lxcTable = createTableTemplate( "Lxc containers", 500 );

        final Button infoBtn = new Button( Buttons.INFO.getButtonLabel() );
        infoBtn.addStyleName( DEFAULT_STYLE_NAME );
        infoBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                getContainerInfo();
            }
        } );

        indicator = new Label();
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );

        GridLayout grid = new GridLayout( 5, 1 );
        grid.setSpacing( true );

        grid.addComponent( infoBtn );
        grid.addComponent( indicator );
        grid.setComponentAlignment( indicator, Alignment.MIDDLE_CENTER );
        addComponent( grid );

        addComponent( lxcTable );

        buildContextMenu();

        addAttachListener( new AttachListener()
        {
            @Override
            public void attach( final AttachEvent event )
            {
                executorService = Executors.newCachedThreadPool();
            }
        } );

        addDetachListener( new DetachListener()
        {
            @Override
            public void detach( final DetachEvent event )
            {
                executorService.shutdown();
            }
        } );
    }


    private void buildContextMenu()
    {
        if ( contextMenu != null )
        {
            lxcTable.removeActionHandler( contextMenu );
        }
        contextMenu = new Action.Handler()
        {
            @Override
            public Action[] getActions( final Object target, final Object sender )
            {
                return getContextActions( target );
            }


            @Override
            public void handleAction( final Action action, final Object sender, final Object target )
            {
                performContextMenuAction( target, action );
            }
        };
        lxcTable.addActionHandler( contextMenu );
    }


    private void performContextMenuAction( final Object target, final Action action )
    {
        Item row = lxcTable.getItem( target );
        try
        {
            if ( !lxcTable.areChildrenAllowed( target ) )
            {
                final String lxcHostname = ( String ) row.getItemProperty( HOST_NAME ).getValue();
                containerHostContextAction( lxcHostname, action );
            }
            else if ( lxcTable.hasChildren( target ) )
            {
                final String physicalHostname = ( String ) row.getItemProperty( HOST_NAME ).getValue();
                resourceHostContextAction( physicalHostname, action );
            }
        }
        catch ( PeerException pe )
        {
            Notification.show( pe.getMessage() );
            LOG.error( "Error applying action on item {}", target );
        }
    }


    private Action[] getContextActions( final Object target )
    {
        if ( target != null )
        {
            if ( !lxcTable.areChildrenAllowed( target ) )
            {
                Item row = lxcTable.getItem( target );
                final String lxcHostname = ( String ) row.getItemProperty( HOST_NAME ).getValue();
                LocalPeer localPeer = peerManager.getLocalPeer();
                return buildSingleItemAction( localPeer, lxcHostname );
            }
            else if ( lxcTable.hasChildren( target ) )
            {
                return new Action[] { START_ALL, STOP_ALL, DESTROY_ALL };
            }
        }
        return new Action[0];
    }


    private Action[] buildSingleItemAction( final LocalPeer localPeer, final String lxcHostname )
    {
        try
        {
            final ContainerHost containerHost = localPeer.getContainerHostByName( lxcHostname );
            if ( containerHost.getStatus() == ContainerHostState.RUNNING )
            {
                return new Action[] { STOP_CONTAINER, DESTROY_CONTAINER, TAG_CONTAINER };
            }
            else if ( containerHost.getStatus() == ContainerHostState.STOPPED )
            {
                return new Action[] { START_CONTAINER, DESTROY_CONTAINER, TAG_CONTAINER };
            }
            else
            {
                return new Action[] { DESTROY_CONTAINER, TAG_CONTAINER };
            }
        }
        catch ( final PeerException e )
        {
            LOG.error( "Error getting container state", e );
            getUI().access( new Runnable()
            {
                @Override
                public void run()
                {
                    Notification.show( e.getMessage() );
                }
            } );
        }
        return new Action[0];
    }


    private void containerHostContextAction( final String lxcHostname, final Action action ) throws PeerException
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        final ContainerHost containerHost = localPeer.getContainerHostByName( lxcHostname );
        if ( action == START_CONTAINER )
        {
            if ( containerHost.getStatus() == ContainerHostState.STOPPED )
            {
                startContainer( containerHost );
            }
        }
        else if ( action == STOP_CONTAINER )
        {
            if ( containerHost.getStatus() == ContainerHostState.RUNNING )
            {
                stopContainer( containerHost );
            }
        }
        else if ( action == DESTROY_CONTAINER )
        {
            ConfirmationDialog alert = new ConfirmationDialog( "Do you want to destroy this container?", "Yes", "No" );
            alert.getOk().addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    destroyContainer( containerHost );
                }
            } );
            getUI().addWindow( alert.getAlert() );
        }
        else if ( action == TAG_CONTAINER )
        {
            getUI().addWindow( new TagsWindow( containerHost ) );
        }
    }


    private void resourceHostContextAction( final String physicalHostname, final Action action ) throws PeerException
    {
        final ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHostByName( physicalHostname );
        if ( resourceHost == null )
        {
            return;
        }
        if ( action == START_ALL )
        {
            startAllActionTrigger( resourceHost.getContainerHosts() );
        }
        else if ( action == STOP_ALL )
        {
            stopAllActionTrigger( resourceHost.getContainerHosts() );
        }
        else if ( action == DESTROY_ALL )
        {
            destroyAllActionTrigger( resourceHost.getContainerHosts() );
        }
    }


    private void startAllActionTrigger( final Set<ContainerHost> containerHosts ) throws PeerException
    {
        for ( ContainerHost containerHost : containerHosts )
        {
            if ( containerHost.getStatus() == ContainerHostState.STOPPED )
            {
                startContainer( containerHost );
            }
        }
    }


    private void stopAllActionTrigger( final Set<ContainerHost> containerHosts ) throws PeerException
    {
        for ( ContainerHost containerHost : containerHosts )
        {
            if ( containerHost.getStatus() == ContainerHostState.RUNNING )
            {
                stopContainer( containerHost );
            }
        }
    }


    private void destroyAllActionTrigger( final Set<ContainerHost> containerHosts )
    {
        ConfirmationDialog alert =
                new ConfirmationDialog( "Do you want to destroy all containers of this resource host?", "Yes", "No" );
        alert.getOk().addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                for ( ContainerHost containerHost : containerHosts )
                {
                    destroyContainer( containerHost );
                }
            }
        } );
        getUI().addWindow( alert.getAlert() );
    }


    private TreeTable createTableTemplate( String caption, int size )
    {
        TreeTable table = new TreeTable( caption );
        table.addContainerProperty( HOST_NAME, String.class, null );
        table.addContainerProperty( LXC_STATUS, Label.class, null );

        table.setWidth( 100, Unit.PERCENTAGE );
        table.setHeight( size, Unit.PIXELS );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );

        return table;
    }


    private void show( String msg )
    {
        Notification.show( msg );
    }


    private void destroyContainer( final ContainerHost containerHost )
    {
        final LocalPeer localPeer = peerManager.getLocalPeer();
        if ( containerHost != null )
        {
            final Runnable updateTable = new Runnable()
            {
                @Override
                public void run()
                {
                    lxcTable.removeItem( containerHost.getHostname() );
                }
            };
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    showHideIndicator( true );
                    try
                    {
                        localPeer.destroyContainer( containerHost.getContainerId() );
                        getUI().access( updateTable );
                    }
                    catch ( PeerException e )
                    {
                        LOG.error( "Error destroying container", e );
                        show( String.format( "Could not destroy container. Error occurred: (%s)", e.toString() ) );
                    }
                    showHideIndicator( false );
                }
            };
            Manager.this.executorService.execute( runnable );
        }
    }


    private void startContainer( final ContainerHost containerHost )
    {
        final LocalPeer localPeer = peerManager.getLocalPeer();
        if ( containerHost != null )
        {
            final Runnable updateViews = new Runnable()
            {
                @Override
                public void run()
                {
                    buildContextMenu();
                    Property lxcStatus = lxcTable.getItem( containerHost.getHostname() ).getItemProperty( LXC_STATUS );
                    Label lbl = ( Label ) lxcStatus.getValue();
                    lbl.setValue( "RUNNING" );
                }
            };
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    showHideIndicator( true );
                    try
                    {
                        localPeer.startContainer( containerHost.getContainerId() );
                        getUI().access( updateViews );
                    }
                    catch ( PeerException e )
                    {
                        LOG.error( "Error starting container", e );
                        show( String.format( "Could not start container. Error occurred: (%s)", e.toString() ) );
                    }
                    showHideIndicator( false );
                }
            };
            Manager.this.executorService.execute( runnable );
        }
    }


    private void stopContainer( final ContainerHost containerHost )
    {

        final LocalPeer localPeer = peerManager.getLocalPeer();
        if ( containerHost != null )
        {
            final Runnable updateViews = new Runnable()
            {
                @Override
                public void run()
                {
                    buildContextMenu();
                    Property lxcStatus = lxcTable.getItem( containerHost.getHostname() ).getItemProperty( LXC_STATUS );
                    Label lbl = ( Label ) lxcStatus.getValue();
                    lbl.setValue( "STOPPED" );
                }
            };
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    showHideIndicator( true );
                    try
                    {
                        localPeer.stopContainer( containerHost.getContainerId() );
                        getUI().access( updateViews );
                    }
                    catch ( PeerException e )
                    {
                        LOG.error( "Error stopping container", e );
                        show( String.format( "Could not stop container. Error occurred: (%s)", e.toString() ) );
                    }

                    showHideIndicator( false );
                }
            };
            Manager.this.executorService.execute( runnable );
        }
    }


    private void showHideIndicator( boolean showHide )
    {
        LOG.debug( "Changing indicator visibility" );
        if ( showHide )
        {
            getUI().access( new Runnable()
            {
                @Override
                public void run()
                {
                    indicator.setVisible( true );
                    PENDING_PROCESSES.incrementAndGet();
                }
            } );
        }
        else
        {
            if ( PENDING_PROCESSES.decrementAndGet() == 0 )
            {
                getUI().access( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        indicator.setVisible( false );
                    }
                } );
            }
        }
    }


    public void getContainerInfo()
    {
        lxcTable.setEnabled( false );
        indicator.setVisible( true );
        final Set<ResourceHost> resourceHosts = peerManager.getLocalPeer().getResourceHosts();
        executorService.execute( new Runnable()
        {
            public void run()
            {
                getUI().access( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        populateTable( resourceHosts );
                        lxcTable.setEnabled( true );
                        indicator.setVisible( false );
                    }
                } );
            }
        } );
    }


    private void populateTable( Set<ResourceHost> resourceHosts )
    {
        final Button btnApplySettings = new Button();
        btnApplySettings.addStyleName( DEFAULT_STYLE_NAME );

        lxcTable.removeAllItems();

        for ( ResourceHost resourceHost : resourceHosts )
        {
            final Object parentId = lxcTable.addItem( new Object[] {
                    resourceHost.getHostname(), new Label()
            }, resourceHost.getHostname() );

            for ( final ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                Label containerStatus = new Label();
                final String lxcHostname = containerHost.getHostname();
                ContainerHostState state;

                state = containerHost.getStatus();

                if ( ContainerHostState.RUNNING.equals( state ) )
                {
                    containerStatus.setValue( "RUNNING" );
                }
                else if ( ContainerHostState.STOPPED.equals( state ) )
                {
                    containerStatus.setValue( "STOPPED" );
                }
                Object childId = lxcTable.addItem( new Object[] {
                        lxcHostname, containerStatus,
                }, lxcHostname );

                lxcTable.setParent( childId, parentId );
                lxcTable.setChildrenAllowed( childId, false );
                lxcTable.setCollapsed( childId, false );

                lxcTable.setCollapsed( parentId, false );
            }
        }
    }
}
