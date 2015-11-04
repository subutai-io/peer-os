package io.subutai.core.environment.ui.forms;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.ContainerType;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateway;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.Template;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.peer.api.PeerManager;


public class BlueprintEditorWindow extends Window
{
    private static final Logger LOG = LoggerFactory.getLogger( BlueprintEditorWindow.class );

    private static final String DEFAULT_SUBNET_CIDR = "192.168.1.2/24";
    private static final Double GB_DIVIDER = 1024.0 * 1024.0 * 1024.0;
    private final Blueprint blueprint;
    private final PeerManager peerManager;
    private final EnvironmentManager environmentManager;
    private final TextField nameTxt;
    private Table placementTable;
    private Button saveBtn;
    private TextField subnetTxt;
    Map<String, ResourceHostMetric> metrics = new ConcurrentHashMap<>();


    public BlueprintEditorWindow( final Blueprint blueprint, final PeerManager peerManager,
                                  final EnvironmentManager environmentManager )
    {

        this.blueprint = blueprint;
        this.peerManager = peerManager;
        this.environmentManager = environmentManager;

        initMetrics();
        setCaption( "Blueprint Editor" );
        setWidth( "800px" );
        setHeight( "600px" );
        setModal( true );
        setClosable( true );

        VerticalLayout content = new VerticalLayout();
        content.setSpacing( true );
        content.setMargin( true );
        content.setStyleName( "default" );
        content.setSizeFull();


        Table nodeGroupsTable = createNodeGroupsTable();

        populateNodeGroupsTable( nodeGroupsTable );

        placementTable = createPlacementTable();

        content.addComponent( nodeGroupsTable );

        content.addComponent( placementTable );

        saveBtn = new Button( "Save" );
        saveBtn.setId( "saveButton" );
        saveBtn.setEnabled( true );
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                //buildProcessTrigger( environmentManager, grow );
                final Blueprint blueprint1 = getBlueprint();
                LOG.info( JsonUtil.toJson( blueprint1 ) );
                try
                {
                    environmentManager.saveBlueprint( blueprint1 );
                    Notification.show( "Blueprint saved successfully." );
                }
                catch ( EnvironmentManagerException e )
                {
                    Notification.show( "Error occured on saving blueprint.", Notification.Type.ERROR_MESSAGE );
                }
            }
        } );

        Button closeBtn = new Button( "Close" );
        closeBtn.setId( "closeButton" );
        closeBtn.setEnabled( true );
        closeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                close();
            }
        } );


        nameTxt = new TextField( "Blueprint name" );
        nameTxt.setId( "nameTxt" );
        nameTxt.setImmediate( true );
        nameTxt.setValue( blueprint.getName() );
        content.addComponent( nameTxt );

        subnetTxt = new TextField( "Subnet CIDR" );
        subnetTxt.setId( "subnetTxt" );
        subnetTxt.setImmediate( true );
        subnetTxt.setValue( blueprint.getCidr() );
        content.addComponent( subnetTxt );


        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing( true );
        buttons.addComponent( saveBtn );
        buttons.addComponent( closeBtn );

        content.addComponent( buttons );

        content.setComponentAlignment( buttons, Alignment.TOP_RIGHT );

        initNodeGroupsTable();

        setContent( content );
    }


    private void initMetrics()
    {
        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                Set<String> peers = new HashSet<String>();
                for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
                {
                    ResourceHostMetric m = new ResourceHostMetric();
                    m.setPeerId( nodeGroup.getPeerId() );
                    m.setHostId( nodeGroup.getHostId() );
                    metrics.put( nodeGroup.getHostId(), m );
                    peers.add( nodeGroup.getPeerId() );
                }

                for ( String peerId : peers )
                {
                    try
                    {
                        Peer peer = peerManager.getPeer( peerId );
                        ResourceHostMetrics rm = peer.getResourceHostMetrics();
                        for ( ResourceHostMetric m : rm.getResources() )
                        {
                            metrics.put( m.getHostId(), m );
                        }
                    }
                    catch ( Exception ignore )
                    {
                        // ignore
                    }
                }
            }
        } ).start();
    }


    private void constructTopology( final Topology topology, final Map<Peer, Set<NodeGroup>> placements )
    {
        for ( Map.Entry<Peer, Set<NodeGroup>> placement : placements.entrySet() )
        {
            for ( NodeGroup nodeGroup : placement.getValue() )
            {
                topology.addNodeGroupPlacement( placement.getKey(), nodeGroup );
            }
        }
    }


    private void checkPickedSubnetValidity( final Topology topology, final EnvironmentManager environmentManager )
            throws EnvironmentManagerException, PeerException, EnvironmentCreationException
    {
        //check availability of subnet
        Map<Peer, Set<Gateway>> usedGateways = getUsedGateways( topology.getNodeGroupPlacement().keySet() );

        SubnetUtils subnetUtils = new SubnetUtils( subnetTxt.getValue() );
        String environmentGatewayIp = subnetUtils.getInfo().getLowAddress();

        for ( Map.Entry<Peer, Set<Gateway>> peerGateways : usedGateways.entrySet() )
        {
            Peer peer = peerGateways.getKey();
            Set<Gateway> gateways = peerGateways.getValue();
            for ( Gateway gateway : gateways )
            {
                if ( gateway.getIp().equals( environmentGatewayIp ) )
                {
                    throw new EnvironmentManagerException(
                            String.format( "Subnet %s is already used on peer %s", environmentGatewayIp,
                                    peer.getName() ), null );
                }
            }
        }

        environmentManager
                .createEnvironment( String.format( "%s-%s", blueprint.getName(), UUID.randomUUID() ), topology,
                        subnetTxt.getValue(), null, true );
        Notification.show( "Environment creation started" );
    }


    private Map<Peer, Set<Gateway>> getUsedGateways( Set<Peer> peers ) throws PeerException
    {
        Map<Peer, Set<Gateway>> usedGateways = Maps.newHashMap();

        for ( Peer peer : peers )
        {
            usedGateways.put( peer, peer.getGateways() );
        }

        return usedGateways;
    }


    private Blueprint getBlueprint()
    {
        Set<NodeGroup> nodeGroups = new HashSet();

        for ( Object itemId : placementTable.getItemIds() )
        {
            Item item = placementTable.getItem( itemId );
            String peerName = item.getItemProperty( "Host" ).getValue().toString();
            int amount = Integer.parseInt( item.getItemProperty( "Amount" ).getValue().toString() );
            String nodeGroupName = item.getItemProperty( "Name" ).getValue().toString();
            String templateName = item.getItemProperty( "Template" ).getValue().toString();
            ContainerType type = ( ContainerType ) item.getItemProperty( "Type" ).getValue();
            String hostId = ( ( ResourceHostMetric ) item.getItemProperty( "Host" ).getValue() ).getHostId();
            String peerId = ( ( ResourceHostMetric ) item.getItemProperty( "Host" ).getValue() ).getPeerId();
            NodeGroup nodeGroup = new NodeGroup( nodeGroupName, templateName, type, amount, 0, 0, peerId, hostId );
            nodeGroups.add( nodeGroup );
        }

        final Blueprint b = new Blueprint( nameTxt.getValue(), subnetTxt.getValue(), nodeGroups );

        b.setId( this.blueprint.getId() );
        b.setType( this.blueprint.getType() );
        return b;
    }


    private Map<Peer, Set<NodeGroup>> getPlacements()
    {
        Map<Peer, Set<NodeGroup>> placements = Maps.newHashMap();

        for ( Object itemId : placementTable.getItemIds() )
        {
            Item item = placementTable.getItem( itemId );
            String nodeGroupName = item.getItemProperty( "Name" ).getValue().toString();
            String peerName = item.getItemProperty( "Host" ).getValue().toString();
            int amount = Integer.parseInt( item.getItemProperty( "Amount" ).getValue().toString() );

            NodeGroup nodeGroup = null;
            for ( NodeGroup ng : blueprint.getNodeGroups() )
            {
                if ( ng.getName().equalsIgnoreCase( nodeGroupName ) )
                {
                    nodeGroup = new NodeGroup( nodeGroupName, ng.getTemplateName(), amount, ng.getSshGroupId(),
                            ng.getHostsGroupId(), ng.getContainerPlacementStrategy() );
                    break;
                }
            }

            Peer peer = null;
            for ( Peer p : peerManager.getPeers() )
            {
                if ( p.getName().equalsIgnoreCase( peerName ) )
                {
                    peer = p;
                    break;
                }
            }

            if ( peer != null && nodeGroup != null )
            {
                Set<NodeGroup> peerNodeGroups = placements.get( peer );

                if ( peerNodeGroups == null )
                {
                    peerNodeGroups = Sets.newHashSet();
                    placements.put( peer, peerNodeGroups );
                }

                peerNodeGroups.add( nodeGroup );
            }
            else
            {
                return null;
            }
        }

        return placements;
    }


    private Table createPlacementTable()
    {
        final Table table = new Table();
        table.addContainerProperty( "Template", Template.class, null );
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "Amount", Integer.class, null );
        table.addContainerProperty( "Peer", String.class, null );
        table.addContainerProperty( "Host", ResourceHostMetric.class, null );
        table.addContainerProperty( "Type", ContainerType.class, null );
        table.addContainerProperty( "Remove", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();

        //        table.setColumnWidth( "Host", 50 );
        table.setItemDescriptionGenerator( new AbstractSelect.ItemDescriptionGenerator()
        {
            @Override
            public String generateDescription( final Component component, final Object itemId, final Object propertyId )
            {
                if ( "Host".equals( propertyId ) )
                {
                    Item item = ( Item ) table.getItem( itemId );
                    Property property = item.getItemProperty( propertyId );
                    ResourceHostMetric metric = ( ResourceHostMetric ) property.getValue();

                    return getResourceHostDescription( metric );
                }
                return null;
            }
        } );
        return table;
    }


    private String getResourceHostDescription( final ResourceHostMetric metric )
    {
        if ( metric == null )
        {
            return "";
        }
        ResourceHostMetric m = metrics.get( metric.getHostId() );
        return String
                .format( "Hostname: %s<br/> RAM: %.3f/%.3fGb<br/> Disk: %.3f/%.3fGb<br/> CPU: %s<br/> Containers #: %d",
                        m.getHostName(), m.getTotalRam() / GB_DIVIDER, m.getFreeRam() / GB_DIVIDER,
                        m.getTotalSpace() / GB_DIVIDER, m.getAvailableSpace() / GB_DIVIDER, m.getCpuModel(),
                        m.getContainersCount() );
    }


    private void populateNodeGroupsTable( final Table nodeGroupsTable )
    {
        //        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
        //        {
        TextField nodeGroupNameField = new TextField( null, "Sample node group" );
        Slider slider = new Slider( 1, 10 );
        slider.setWidth( 100, Unit.PIXELS );
        slider.setOrientation( SliderOrientation.HORIZONTAL );
        slider.setValue( ( double ) 1 );

        ComboBox peersCombo = createPeersComboBox();
        peersCombo.setId( "peersCombo" );
        ComboBox typesCombo = createTypesComboBox();
        typesCombo.setId( "typesCombo" );

        ComboBox templatesCombo = createTemplatesComboBox();
        typesCombo.setId( "templatesCombo" );

        Button placeBtn = createPlaceButton( templatesCombo, nodeGroupNameField, slider, typesCombo, peersCombo );

        nodeGroupsTable.addItem( new Object[] {
                templatesCombo, nodeGroupNameField, slider, peersCombo, typesCombo, placeBtn
        }, null );
        //        }
    }


    private Button createPlaceButton( final ComboBox templatesCombo, final TextField nodeGroup, final Slider slider,
                                      final ComboBox type, final ComboBox peersCombo )
    {
        Button placeButton = new Button( "Place" );
        placeButton.setId( "placeButton" );

        placeButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                if ( peersCombo.getValue() == null )
                {

                    Notification.show( "Please, select target peer", Notification.Type.WARNING_MESSAGE );
                }
                else if ( slider.getMax() == 0 )
                {
                    Notification.show( "All containers are distributed", Notification.Type.WARNING_MESSAGE );
                }
                else if ( slider.getValue().intValue() == 0 )
                {
                    Notification.show( "Select number of nodes to place", Notification.Type.WARNING_MESSAGE );
                }
                else
                {
                    placeNodeGroup( templatesCombo, nodeGroup, slider, type,
                            ( ResourceHostMetric ) peersCombo.getValue() );
                }
            }
        } );

        return placeButton;
    }


    private void placeNodeGroup( final ComboBox templatesCombo, TextField nodeGroupNameField, final Slider slider,
                                 ComboBox type, ResourceHostMetric metric )
    {
        //TODO: check uniqueness of nodeGroupName
        final String nodeGroupName = nodeGroupNameField.getValue();

        final String rowId = String.format( "%s-%s-%s", nodeGroupName, metric.getPeerId(), metric.getHostId() );

        Button removeBtn = new Button( "Remove" );
        removeBtn.setId( "removeButton" );


        final int amount = slider.getValue().intValue();
        removeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                Item row = placementTable.getItem( rowId );
                slider.setMax(
                        slider.getMax() + Integer.parseInt( row.getItemProperty( "Amount" ).getValue().toString() ) );
                slider.setValue( slider.getMax() );
                slider.setMin( 1 );
                placementTable.removeItem( rowId );
            }
        } );

        final Item row = placementTable.getItem( rowId );

        if ( row == null )
        {
            placementTable.addItem( new Object[] {
                    templatesCombo.getValue(), nodeGroupName, amount,
                    peerManager.getPeer( metric.getPeerId() ).getPeerInfo().getIp(), metric, type.getValue(), removeBtn
            }, rowId );
        }
        else
        {
            row.getItemProperty( "Amount" )
               .setValue( amount + Integer.parseInt( row.getItemProperty( "Amount" ).getValue().toString() ) );
        }

        double newMax = slider.getMax() - amount;
        slider.setValue( 1d );
        slider.setMin( Math.min( slider.getMin(), newMax ) );
        slider.setMax( newMax );
        slider.setValue( newMax );
    }


    private void initNodeGroupsTable()
    {
        for ( NodeGroup nodeGroup : this.blueprint.getNodeGroups() )
        {
            final String nodeGroupName = nodeGroup.getName();

            String peerId = nodeGroup.getPeerId();
            String hostId = nodeGroup.getHostId();
            Peer peer = peerManager.getPeer( peerId );
            ResourceHostMetric metric = metrics.get( hostId );
            int amount = nodeGroup.getNumberOfContainers();
            ContainerType type = nodeGroup.getType();
            final String rowId = String.format( "%s-%s-%s", nodeGroupName, peerId, hostId );


            Button removeBtn = new Button( "Remove" );
            removeBtn.setId( "removeButton-" + rowId );


            removeBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent event )
                {
                    Item row = placementTable.getItem( rowId );
                    placementTable.removeItem( rowId );
                }
            } );


            placementTable.addItem( new Object[] {
                    peerManager.getTemplateByName( nodeGroup.getTemplateName() ), nodeGroupName, amount,
                    peer.getPeerInfo().getIp(), metric, type, removeBtn
            }, rowId );
        }
    }


    private ComboBox createPeersComboBox()
    {
        ComboBox peersCombo = new ComboBox();
        peersCombo.setNullSelectionAllowed( false );
        peersCombo.setTextInputAllowed( false );
        peersCombo.setImmediate( true );
        peersCombo.setRequired( true );
        List<Peer> peers = peerManager.getPeers();

        for ( Peer peer : peers )
        {
            PeerInfo peerInfo = peer.getPeerInfo();
            ResourceHostMetrics metrics = peer.getResourceHostMetrics();
            for ( ResourceHostMetric m : metrics.getResources() )
            {
                peersCombo.addItem( m );

                String caption =
                        String.format( "%s on %s: %s %.3fGb", m.getHostName(), peerInfo.getIp(), m.getCpuModel(),
                                m.getTotalRam() / 1024 / 1024 / 1024 );
                peersCombo.setItemCaption( m, caption );
            }
        }

        peersCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                ResourceHostMetric m = ( ResourceHostMetric ) event.getProperty().getValue();
                Notification.show( m.getHostName() );
            }
        } );

        return peersCombo;
    }


    private ComboBox createTypesComboBox()
    {
        ComboBox typesCombo = new ComboBox();
        typesCombo.setNullSelectionAllowed( false );
        typesCombo.setTextInputAllowed( false );
        typesCombo.setImmediate( true );
        typesCombo.setRequired( true );

        for ( ContainerType t : ContainerType.values() )
        {
            typesCombo.addItem( t );
        }

        return typesCombo;
    }


    private ComboBox createTemplatesComboBox()
    {
        ComboBox typesCombo = new ComboBox();
        typesCombo.setNullSelectionAllowed( false );
        typesCombo.setTextInputAllowed( false );
        typesCombo.setImmediate( true );
        typesCombo.setRequired( true );

        for ( Template t : peerManager.getTemplates() )
        {
            typesCombo.addItem( t );
        }

        return typesCombo;
    }


    private Table createNodeGroupsTable()
    {
        Table table = new Table();
        table.addContainerProperty( "Template", ComboBox.class, null );
        table.addContainerProperty( "Name", TextField.class, null );
        table.addContainerProperty( "Amount", Slider.class, null );
        table.addContainerProperty( "Host", ComboBox.class, null );
        table.addContainerProperty( "Type", ComboBox.class, null );
        table.addContainerProperty( "Place", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }
}
