package org.safehaus.subutai.core.peer.ui.container.manage;


import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.ui.container.common.Buttons;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
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


@SuppressWarnings( "serial" )
public class Manager extends VerticalLayout
{

    private static final String HOST_NAME = "Host name";
    private static final String LXC_STATUS = "Status";


    private final Label indicator;
    private final TreeTable lxcTable;
    private final PeerManager peerManager;
    private final ExecutorService executorService;

    private static final AtomicInteger processPending = new AtomicInteger( 0 );

    private static final Action START_CONTAINER = new Action( "Start" );
    private static final Action STOP_CONTAINER = new Action( "Stop" );
    private static final Action DESTROY_CONTAINER = new Action( "Destroy" );

    private static final Action START_ALL = new Action( "Start All" );
    private static final Action STOP_ALL = new Action( "Stop All" );
    private static final Action DESTROY_ALL = new Action( "Destroy All" );

    private static final Logger LOG = LoggerFactory.getLogger( Manager.class );


    public Manager( final ExecutorService executorService, final PeerManager peerManager )
    {

        this.executorService = executorService;

        setSpacing( true );
        setMargin( true );

        this.peerManager = peerManager;

        lxcTable = createTableTemplate( "Lxc containers", 500 );

        final Button infoBtn = new Button( Buttons.INFO.getButtonLabel() );
        infoBtn.addStyleName( "default" );
        infoBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                getContainerInfo();
            }
        } );
        final Button cleanDbBtn = new Button( Buttons.CLEAN_DB.getButtonLabel() );
        cleanDbBtn.addStyleName( "default" );
        cleanDbBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                peerManager.getLocalPeer().cleanDb();
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
        grid.addComponent( cleanDbBtn );
        grid.addComponent( indicator );
        grid.setComponentAlignment( indicator, Alignment.MIDDLE_CENTER );
        addComponent( grid );

        final Action.Handler actionHandler = new Action.Handler()
        {
            @Override
            public Action[] getActions( final Object target, final Object sender )
            {
                if ( target != null )
                {
                    if ( !lxcTable.areChildrenAllowed( target ) )
                    {
                        return new Action[] { START_CONTAINER, STOP_CONTAINER, DESTROY_CONTAINER };
                    }
                    else
                    {
                        return new Action[] { START_ALL, STOP_ALL, DESTROY_ALL };
                    }
                }
                return new Action[0];
            }


            @Override
            public void handleAction( final Action action, final Object sender, final Object target )
            {
                Item row = lxcTable.getItem( target );
                try
                {
                    if ( !lxcTable.hasChildren( target ) )
                    {
                        final String lxcHostname = ( String ) row.getItemProperty( HOST_NAME ).getValue();
                        LocalPeer localPeer = peerManager.getLocalPeer();
                        final ContainerHost containerHost = localPeer.getContainerHostByName( lxcHostname );
                        if ( action == START_CONTAINER )
                        {
                            startContainer( containerHost );
                        }
                        else if ( action == STOP_CONTAINER )
                        {
                            stopContainer( containerHost );
                        }
                        else if ( action == DESTROY_CONTAINER )
                        {
                            ConfirmationDialog alert =
                                    new ConfirmationDialog( "Do you want to destroy this lxc node?", "Yes", "No" );
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
                    }
                    else
                    {
                        final String physicalHostname = ( String ) row.getItemProperty( HOST_NAME ).getValue();
                        final ResourceHost resourceHost =
                                peerManager.getLocalPeer().getResourceHostByName( physicalHostname );
                        if ( resourceHost == null )
                        {
                            return;
                        }
                        if ( action == START_ALL )
                        {
                            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
                            {
                                startContainer( containerHost );
                            }
                        }
                        else if ( action == STOP_ALL )
                        {
                            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
                            {
                                stopContainer( containerHost );
                            }
                        }
                        else if ( action == DESTROY_ALL )
                        {
                            ConfirmationDialog alert =
                                    new ConfirmationDialog( "Do you want to destroy this lxc node?", "Yes", "No" );
                            alert.getOk().addClickListener( new Button.ClickListener()
                            {
                                @Override
                                public void buttonClick( Button.ClickEvent clickEvent )
                                {
                                    for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
                                    {
                                        destroyContainer( containerHost );
                                    }
                                }
                            } );
                            getUI().addWindow( alert.getAlert() );
                        }
                    }
                }
                catch ( PeerException pe )
                {
                    pe.printStackTrace();
                }
            }
        };
        lxcTable.addActionHandler( actionHandler );

        addComponent( lxcTable );
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
            Manager.this.executorService.execute( new Runnable()
            {
                public void run()
                {
                    showHideIndicator( true );
                    try
                    {
                        localPeer.destroyContainer( containerHost );
                        getUI().access( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                lxcTable.removeItem( containerHost.getHostname() );
                            }
                        } );
                    }
                    catch ( PeerException e )
                    {
                        show( String.format( "Could not destroy container. Error occurred: (%s)", e.toString() ) );
                    }
                    showHideIndicator( false );
                }
            } );
        }
    }


    private void startContainer( final ContainerHost containerHost )
    {
        final LocalPeer localPeer = peerManager.getLocalPeer();
        if ( containerHost != null )
        {
            Manager.this.executorService.execute( new Runnable()
            {
                public void run()
                {
                    showHideIndicator( true );
                    try
                    {
                        localPeer.startContainer( containerHost );
                        getUI().access( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Property lxcStatus =
                                        lxcTable.getItem( containerHost.getHostname() ).getItemProperty( LXC_STATUS );
                                Label lbl = ( Label ) lxcStatus.getValue();
                                lbl.setValue( "RUNNING" );
                            }
                        } );
                    }
                    catch ( PeerException e )
                    {
                        show( String.format( "Could not start container. Error occurred: (%s)", e.toString() ) );
                    }
                    showHideIndicator( false );
                }
            } );
        }
    }


    private void stopContainer( final ContainerHost containerHost )
    {

        final LocalPeer localPeer = peerManager.getLocalPeer();
        if ( containerHost != null )
        {
            Manager.this.executorService.execute( new Runnable()
            {
                public void run()
                {
                    showHideIndicator( true );
                    try
                    {
                        localPeer.stopContainer( containerHost );
                        getUI().access( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Property lxcStatus =
                                        lxcTable.getItem( containerHost.getHostname() ).getItemProperty( LXC_STATUS );
                                Label lbl = ( Label ) lxcStatus.getValue();
                                lbl.setValue( "STOPPED" );
                            }
                        } );
                    }
                    catch ( PeerException e )
                    {
                        show( String.format( "Could not stop container. Error occurred: (%s)", e.toString() ) );
                    }

                    showHideIndicator( false );
                }
            } );
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
                    processPending.incrementAndGet();
                }
            } );
        }
        else
        {
            if ( processPending.decrementAndGet() == 0 )
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
        btnApplySettings.addStyleName( "default" );

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
                try
                {
                    state = containerHost.getState();
                }
                catch ( PeerException e )
                {
                    state = ContainerHostState.STOPPED;
                }
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
