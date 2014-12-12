package org.safehaus.subutai.core.peer.ui.container.manage;


import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.common.quota.*;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostState;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.*;
import org.safehaus.subutai.core.peer.ui.container.common.Buttons;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;


@SuppressWarnings( "serial" )
public class Manager extends VerticalLayout
{

    //    private static final String PHYSICAL_HOST_LABEL = "Physical Host";
    private static final String HOST_NAME = "Host name";
    private static final String LXC_STATUS = "Status";
    private static final String LXC_MEMORY = "Memory";
    private static final String LXC_CPU_LIST = "Cores used";
    private static final String LXC_UPDATE = "Update Changes";


    private final Label indicator;
    private final Button infoBtn;
    private final TreeTable lxcTable;
    private final PeerManager peerManager;
    //    private final AgentManager agentManager;
    private final QuotaManager quotaManager;
    private final ExecutorService executorService;

    private static final AtomicInteger processPending = new AtomicInteger( 0 );

    private static final Action START_CONTAINER = new Action( "Start" );
    private static final Action STOP_CONTAINER = new Action( "Stop" );
    private static final Action DESTROY_CONTAINER = new Action( "Destroy" );

    private static final Action START_ALL = new Action( "Start All" );
    private static final Action STOP_ALL = new Action( "Stop All" );
    private static final Action DESTROY_ALL = new Action( "Destroy All" );

    private static final Logger LOG = LoggerFactory.getLogger( Manager.class );


    private Action.Handler actionHandler = new Action.Handler()
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


    public Manager( final ExecutorService executorService,/* final AgentManager agentManager,*/
                    QuotaManager quotaManager, PeerManager peerManager )
    {

        this.executorService = executorService;

        setSpacing( true );
        setMargin( true );

        //        this.agentManager = agentManager;
        this.peerManager = peerManager;

        lxcTable = createTableTemplate( "Lxc containers", 500 );

        infoBtn = new Button( Buttons.INFO.getButtonLabel() );
        infoBtn.addStyleName( "default" );
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

        lxcTable.addActionHandler( actionHandler );

        addComponent( lxcTable );
        this.quotaManager = quotaManager;
    }


    private TreeTable createTableTemplate( String caption, int size )
    {
        TreeTable table = new TreeTable( caption );
        table.addContainerProperty( HOST_NAME, String.class, null );
        table.addContainerProperty( LXC_STATUS, Label.class, null );
        //table.addContainerProperty( LXC_MEMORY, QuotaMemoryComponent.class, null );
        //table.addContainerProperty( LXC_CPU_LIST, TextField.class, null );
        table.addContainerProperty( LXC_UPDATE, Button.class, null );

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
                    resourceHost.getHostname(), new Label(), /*null, null,*/ null
            }, resourceHost.getHostname() );

            for ( final ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                boolean emptyTree = true;
                Label containerStatus = new Label();
                Button updateQuota = new Button( "Update" );
                updateQuota.addStyleName( "default" );
                PeerQuotaInfo containerMemory;
                final QuotaMemoryComponent memoryQuotaComponent = new QuotaMemoryComponent();
                final QuotaComponents modifyQuota = new QuotaComponents();

                //                String containerCpu = "Cpu Shares";
                final TextField containerCpuTextField = new TextField();
                final String lxcHostname = containerHost.getHostname();
                ContainerHostState state = null;
                try
                {
                    state = containerHost.getState();
                }
                catch ( PeerException e )
                {
                    state = ContainerHostState.STOPPED;
                }
                if ( ContainerState.RUNNING.equals( state ) )
                {
                    containerStatus.setValue( "RUNNING" );
                    LOG.info( "This is quota manager: " + quotaManager.toString() );

                    try
                    {
                        containerMemory = containerHost.getQuota( QuotaType.QUOTA_ALL_JSON );
                        containerCpuTextField.setValue( containerMemory.getCpuQuotaInfo().getQuotaValue() );

                        modifyQuota.setValueForMemoryTextField2( containerMemory.getMemoryQuota().getQuotaValue() );
                        modifyQuota.setValueForCoresUsedTextField( containerMemory.getCpuQuotaInfo().getQuotaValue() );

                        memoryQuotaComponent
                                .setValueForMemoryTextField( containerMemory.getMemoryQuota().getQuotaValue() );
                        updateQuota.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                Window subWindowModifyLXC = new Window( "Modify LXC container" );
                                AbsoluteLayout mainLayout = new AbsoluteLayout();
                                subWindowModifyLXC.setModal( true );
                                subWindowModifyLXC.center();
                                subWindowModifyLXC.setHeight( "600px" );
                                subWindowModifyLXC.setWidth( "550px" );

                                mainLayout.addComponent( modifyQuota, "top:0.0px;right:0.0px;left:0.0px;" );

                                btnApplySettings.addStyleName( "default" );
                                btnApplySettings.setCaption( "Apply" );
                                btnApplySettings.setImmediate( true );
                                btnApplySettings.setWidth( "-1px" );
                                btnApplySettings.setHeight( "-1px" );
                                mainLayout.addComponent( btnApplySettings, "top:500.0px;left:450.0px;" );

                                subWindowModifyLXC.setContent( mainLayout );

                                UI.getCurrent().addWindow( subWindowModifyLXC );
                            }
                        } );
                        btnApplySettings.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                try
                                {
                                    String memoryLimit = modifyQuota.getMemoryLimitValue();
                                    String cpuLimit = modifyQuota.getValueFromCpuCoresUsed();

//                                    Memory memory = new Memory( memoryLimit );
                                    QuotaInfo memoryQuota = new MemoryQuotaInfo( memoryLimit );
                                    QuotaInfo cpuQuota = new CpuQuotaInfo( cpuLimit );

                                    containerHost.setQuota( memoryQuota );
                                    containerHost.setQuota( cpuQuota );
                                }
                                catch ( PeerException pe )
                                {
                                    show( pe.toString() );
                                }
                                Notification.show( "Settings applied" );
                            }
                        } );
                    }
                    catch ( PeerException pe )
                    {
                        show( pe.toString() );
                    }
                }
                else if ( ContainerState.STOPPED.equals( state ) )
                {
                    containerStatus.setValue( "STOPPED" );
                }
                Object childId = lxcTable.addItem( new Object[] {
                        lxcHostname, containerStatus, /*memoryQuotaComponent, containerCpuTextField,*/ updateQuota
                }, lxcHostname );

                lxcTable.setParent( childId, parentId );
                lxcTable.setChildrenAllowed( childId, false );
                lxcTable.setCollapsed( childId, false );
                emptyTree = false;
                if ( emptyTree )
                {
                    lxcTable.removeItem( parentId );
                }
                lxcTable.setCollapsed( parentId, false );
            }
        }
    }


    private void populateTableOld( Map<String, EnumMap<ContainerState, List<String>>> agentFamilies )
    {
        lxcTable.removeAllItems();

        for ( Map.Entry<String, EnumMap<ContainerState, List<String>>> agentFamily : agentFamilies.entrySet() )
        {
            boolean emptyTree = true;
            final String parentHostname = agentFamily.getKey();
            final Object parentId = lxcTable.addItem( new Object[] {
                    parentHostname, new Label(), null, null, null
            }, parentHostname );

            for ( Map.Entry<ContainerState, List<String>> lxcs : agentFamily.getValue().entrySet() )
            {
                for ( final String lxcHostname : lxcs.getValue() )
                {
                    Label containerStatus = new Label();
                    Button updateQuota = new Button( "Update" );
                    updateQuota.addStyleName( "default" );
                    String containerMemory;
                    final QuotaMemoryComponent memoryQuotaComponent = new QuotaMemoryComponent();

                    String containerCpu = "Cpu Shares";
                    final TextField containerCpuTextField = new TextField();

                    if ( lxcs.getKey() == ContainerState.RUNNING )
                    {
                        containerStatus.setValue( "RUNNING" );
                        LOG.info( "This is quota manager: " + quotaManager.toString() );

                        //                        try
                        //                        {
                        ////                            containerMemory = quotaManager.getQuota( lxcHostname,
                        // QuotaEnum.MEMORY_LIMIT_IN_BYTES,
                        ////                                    agentManager.getAgentByHostname( parentHostname ) );
                        ////                            containerCpu = quotaManager.getQuota( lxcHostname, QuotaEnum
                        // .CPUSET_CPUS,
                        ////                                    agentManager.getAgentByHostname( parentHostname ) );
                        ////                            containerCpuTextField.setValue( containerCpu );
                        //
                        ////                            memoryQuotaComponent.setValueForMemoryTextField(
                        // containerMemory );
                        ////                            updateQuota.addClickListener( new Button.ClickListener()
                        ////                            {
                        ////                                @Override
                        ////                                public void buttonClick( final Button.ClickEvent
                        // clickEvent )
                        ////                                {
                        ////                                    try
                        ////                                    {
                        ////                                        String memoryLimit = memoryQuotaComponent
                        // .getMemoryLimitValue();
                        ////                                        String cpuLimit = containerCpuTextField.getValue
                        // ().replaceAll( "\n", "" );
                        ////
                        ////                                        quotaManager
                        ////                                                .setQuota( lxcHostname, QuotaEnum
                        // .MEMORY_LIMIT_IN_BYTES, memoryLimit,
                        ////                                                        agentManager.getAgentByHostname(
                        // parentHostname ) );
                        ////                                        quotaManager.setQuota( lxcHostname, QuotaEnum
                        // .CPUSET_CPUS, cpuLimit,
                        ////                                                agentManager.getAgentByHostname(
                        // parentHostname ) );
                        ////                                    }
                        ////                                    catch ( QuotaException e )
                        ////                                    {
                        ////                                        LOG.error( "Error executing command lxc-cgroup
                        // -n:", e );
                        ////                                    }
                        ////                                }
                        ////                            } );
                        //                        }
                        //                        catch ( QuotaException e )
                        //                        {
                        //                            LOG.error( "Error executing command lxc-cgroup -n: ", e );
                        //                        }
                    }
                    else if ( lxcs.getKey() == ContainerState.STOPPED )
                    {
                        containerStatus.setValue( "STOPPED" );
                    }
                    Object childId = lxcTable.addItem( new Object[] {
                            lxcHostname, containerStatus, memoryQuotaComponent, containerCpuTextField, updateQuota
                    }, lxcHostname );

                    lxcTable.setParent( childId, parentId );
                    lxcTable.setChildrenAllowed( childId, false );
                    lxcTable.setCollapsed( childId, false );
                    emptyTree = false;
                }
            }
            if ( emptyTree )
            {
                lxcTable.removeItem( parentId );
            }
            lxcTable.setCollapsed( parentId, false );
        }
    }
}
