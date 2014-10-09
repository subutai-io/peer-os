package org.safehaus.subutai.core.container.ui.manage;


import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.ContainerDestroyException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.container.api.ContainerState;
import org.safehaus.subutai.core.container.ui.common.Buttons;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.lxc.quota.api.QuotaException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
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
    private final ContainerManager containerManager;
    private final AgentManager agentManager;
    private final QuotaManager quotaManager;
    private final ExecutorService executorService;

    private static final AtomicInteger processPending = new AtomicInteger( 0 );

    private static final Action START_CONTAINER = new Action( "Start" );
    private static final Action STOP_CONTAINER = new Action( "Stop" );
    private static final Action DESTROY_CONTAINER = new Action( "Destroy" );

    private static final Action START_ALL = new Action( "Start All" );
    private static final Action STOP_ALL = new Action( "Stop All" );
    private static final Action DESTROY_ALL = new Action( "Destroy All" );

    private static final Logger LOGGER = LoggerFactory.getLogger( Manager.class );


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
            Object parentId = lxcTable.getParent( target );
            Item parentRow = lxcTable.getItem( parentId );

            if ( !lxcTable.hasChildren( target ) )
            {

                final String lxcHostname = ( String ) row.getItemProperty( HOST_NAME ).getValue();
                final String physicalHostname = ( String ) parentRow.getItemProperty( HOST_NAME ).getValue();


                if ( action == START_CONTAINER )
                {
                    startContainer( physicalHostname, lxcHostname );
                }
                else if ( action == STOP_CONTAINER )
                {
                    stopContainer( physicalHostname, lxcHostname );
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
                            destroyContainer( physicalHostname, lxcHostname );
                        }
                    } );
                    getUI().addWindow( alert.getAlert() );
                }
            }
            else
            {
                final String physicalHostname = ( String ) row.getItemProperty( HOST_NAME ).getValue();
                if ( action == START_ALL )
                {
                    Map<String, EnumMap<ContainerState, List<String>>> agentFamilies =
                            Manager.this.containerManager.getContainersOnPhysicalServers();

                    for ( Map.Entry<ContainerState, List<String>> lxcs : agentFamilies.get( physicalHostname )
                                                                                      .entrySet() )
                    {
                        for ( final String lxcHostname : lxcs.getValue() )
                        {
                            startContainer( physicalHostname, lxcHostname );
                        }
                    }
                }
                else if ( action == STOP_ALL )
                {
                    Map<String, EnumMap<ContainerState, List<String>>> agentFamilies =
                            Manager.this.containerManager.getContainersOnPhysicalServers();
                    EnumMap<ContainerState, List<String>> temp = agentFamilies.get( physicalHostname );
                    Collection<List<String>> containerNames = temp.values();
                    for ( List<String> containerName : containerNames )
                    {
                        for ( String lxcHostname : containerName )
                        {
                            stopContainer( physicalHostname, lxcHostname );
                        }
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
                            Map<String, EnumMap<ContainerState, List<String>>> agentFamilies =
                                    Manager.this.containerManager.getContainersOnPhysicalServers();

                            for ( Map.Entry<ContainerState, List<String>> lxcs : agentFamilies.get( physicalHostname )
                                                                                              .entrySet() )
                            {
                                for ( final String lxcHostname : lxcs.getValue() )
                                {
                                    destroyContainer( physicalHostname, lxcHostname );
                                }
                            }
                        }
                    } );
                    getUI().addWindow( alert.getAlert() );
                }
            }
        }
    };


    public Manager( final ExecutorService executorService, final AgentManager agentManager,
                    ContainerManager containerManager, QuotaManager quotaManager )
    {

        this.executorService = executorService;

        setSpacing( true );
        setMargin( true );

        this.agentManager = agentManager;
        this.containerManager = containerManager;

        lxcTable = createTableTemplate( "Lxc containers", 500 );

        infoBtn = new Button( Buttons.INFO.getButtonLabel() );
        infoBtn.addStyleName( "default" );
        infoBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                getLxcInfo();
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
        table.addContainerProperty( LXC_MEMORY, QuotaMemoryComponent.class, null );
        table.addContainerProperty( LXC_CPU_LIST, TextField.class, null );
        table.addContainerProperty( LXC_UPDATE, Button.class, null );

        table.setWidth( 100, Unit.PERCENTAGE );
        table.setHeight( size, Unit.PIXELS );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );

        return table;
    }


    private void destroyContainer( String physicalHostname, final String lxcHostname )
    {
        showHideIndicator( true );
        final Agent physicalAgent = Manager.this.agentManager.getAgentByHostname( physicalHostname );
        if ( physicalAgent != null )
        {
            executorService.execute( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Manager.this.containerManager.destroy( physicalAgent.getHostname(), lxcHostname );
                        getUI().access( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //remove row
                                Object parentId = lxcTable.getParent( lxcHostname );
                                lxcTable.removeItem( lxcHostname );
                                if ( !lxcTable.hasChildren( parentId ) )
                                {
                                    lxcTable.removeItem( parentId );
                                }
                            }
                        } );
                        showHideIndicator( false );
                    }
                    catch ( ContainerDestroyException cde )
                    {
                        //some nasty logs come here from destroying container
                        //so just ignoring these files
                        //                        LOGGER.error( "Error destroying container:", cde );
                    }
                }
            } );
        }
    }


    private void startContainer( String parentHostname, final String lxcHostname )
    {
        LOGGER.info( "Getting parent caption: " + parentHostname );
        showHideIndicator( true );
        final Agent physicalAgent = Manager.this.agentManager.getAgentByHostname( parentHostname );
        if ( physicalAgent != null )
        {
            Manager.this.executorService.execute( new Runnable()
            {
                public void run()
                {
                    Manager.this.containerManager.startLxcOnHost( physicalAgent, lxcHostname );
                    getUI().access( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Property property = lxcTable.getItem( lxcHostname ).getItemProperty( LXC_STATUS );
                            Label label = ( Label ) property.getValue();
                            label.setValue( "RUNNING" );
                        }
                    } );
                    showHideIndicator( false );
                }
            } );
        }
    }


    private void stopContainer( String physicalHostname, final String lxcHostname )
    {
        LOGGER.info( "Getting parent caption: " + physicalHostname );
        showHideIndicator( true );

        final Agent physicalAgent = Manager.this.agentManager.getAgentByHostname( physicalHostname );
        if ( physicalAgent != null )
        {
            Manager.this.executorService.execute( new Runnable()
            {
                public void run()
                {
                    Manager.this.containerManager.stopLxcOnHost( physicalAgent, lxcHostname );
                    getUI().access( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Property lxcStatus = lxcTable.getItem( lxcHostname ).getItemProperty( LXC_STATUS );
                            Label lbl = ( Label ) lxcStatus.getValue();
                            lbl.setValue( "STOPPED" );
                        }
                    } );
                    showHideIndicator( false );
                }
            } );
        }
    }


    private void showHideIndicator( boolean showHide )
    {
        LOGGER.info( "Changing indicator visibility" );
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


    public void getLxcInfo()
    {
        lxcTable.setEnabled( false );
        indicator.setVisible( true );
        executorService.execute( new Runnable()
        {
            public void run()
            {
                final Map<String, EnumMap<ContainerState, List<String>>> agentFamilies;
                agentFamilies = containerManager.getContainersOnPhysicalServers();
                getUI().access( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        populateTable( agentFamilies );
                        lxcTable.setEnabled( true );
                        indicator.setVisible( false );
                    }
                } );
            }
        } );
    }


    private void populateTable( Map<String, EnumMap<ContainerState, List<String>>> agentFamilies )
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
                        LOGGER.info( "This is quota manager: " + quotaManager.toString() );

                        try
                        {
                            containerMemory = quotaManager.getQuota( lxcHostname, QuotaEnum.MEMORY_LIMIT_IN_BYTES,
                                    agentManager.getAgentByHostname( parentHostname ) );
                            containerCpu = quotaManager.getQuota( lxcHostname, QuotaEnum.CPUSET_CPUS,
                                    agentManager.getAgentByHostname( parentHostname ) );
                            containerCpuTextField.setValue( containerCpu );

                            memoryQuotaComponent.setValueForMemoryTextField( containerMemory );
                            updateQuota.addClickListener( new Button.ClickListener()
                            {
                                @Override
                                public void buttonClick( final Button.ClickEvent clickEvent )
                                {
                                    try
                                    {
                                        String memoryLimit = memoryQuotaComponent.getMemoryLimitValue();
                                        String cpuLimit = containerCpuTextField.getValue().replaceAll( "\n", "" );

                                        quotaManager
                                                .setQuota( lxcHostname, QuotaEnum.MEMORY_LIMIT_IN_BYTES, memoryLimit,
                                                        agentManager.getAgentByHostname( parentHostname ) );
                                        quotaManager.setQuota( lxcHostname, QuotaEnum.CPUSET_CPUS, cpuLimit,
                                                agentManager.getAgentByHostname( parentHostname ) );
                                    }
                                    catch ( QuotaException e )
                                    {
                                        LOGGER.error( "Error executing command lxc-cgroup -n:", e );
                                    }
                                }
                            } );
                        }
                        catch ( QuotaException e )
                        {
                            LOGGER.error( "Error executing command lxc-cgroup -n: ", e );
                        }
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
