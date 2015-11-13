package io.subutai.core.environment.ui.forms;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.peer.api.PeerManager;


public class HostEnvironmentBuilderWindow extends Window
{
    private static final Logger LOG = LoggerFactory.getLogger( HostEnvironmentBuilderWindow.class );

    private static final String DEFAULT_SUBNET_CIDR = "192.168.1.2/24";
    private final Blueprint blueprint;
    private final PeerManager peerManager;
    private Table placementTable;
    private Button buildBtn;
    private ComboBox envCombo;
    private TextField subnetTxt;

    List<ResourceHostMetric> metrics = new ArrayList<>();


    public HostEnvironmentBuilderWindow( final Blueprint blueprint, final PeerManager peerManager,
                                         final EnvironmentManager environmentManager, final boolean grow )
    {

        this.blueprint = blueprint;
        this.peerManager = peerManager;
        initMetrics();

        setCaption( "Host based environment builder" );
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

        buildBtn = new Button( grow ? "Expand" : "Build" );
        buildBtn.setId( "buildButton" );
        buildBtn.setEnabled( false );
        buildBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                buildProcessTrigger( environmentManager, grow );
            }
        } );

        if ( grow )
        {
            envCombo = new ComboBox( "Environments" );
            envCombo.setId( "envCb" );
            envCombo.setImmediate( true );
            envCombo.setTextInputAllowed( false );
            envCombo.setNullSelectionAllowed( false );
            envCombo.setRequired( true );
            for ( Environment environment : environmentManager.getEnvironments() )
            {
                envCombo.addItem( environment );
                envCombo.setItemCaption( environment, environment.getName() );
            }
            content.addComponent( envCombo );
        }
        else
        {
            subnetTxt = new TextField( "Subnet CIDR" );
            subnetTxt.setId( "subnetTxt" );
            subnetTxt.setImmediate( true );
            subnetTxt.setValue( DEFAULT_SUBNET_CIDR );
            content.addComponent( subnetTxt );
        }
        content.addComponent( buildBtn );
        content.setComponentAlignment( buildBtn, Alignment.TOP_RIGHT );

        setContent( content );
    }


    private void buildProcessTrigger( final EnvironmentManager environmentManager, final boolean grow )
    {
        Set<NodeGroup> placements = getPlacements();

        if ( placements == null )
        {
            Notification.show( "Failed to obtain topology", Notification.Type.ERROR_MESSAGE );
        }
        else if ( grow && envCombo.getValue() == null )
        {
            Notification.show( "Please, select environment to grow" );
        }
        else if ( !grow && Strings.isNullOrEmpty( subnetTxt.getValue() ) )
        {
            Notification.show( "Please, enter subnet CIDR" );
        }
        else
        {
            Topology topology;
            if ( grow )
            {
                Environment environment = ( Environment ) envCombo.getValue();
                topology = new Topology( environment.getName(), environment.getId(), environment.getSubnetCidr(), null );
            }
            else
            {
                final String environmentId = UUID.randomUUID().toString();
                final String subnet = subnetTxt.getValue().trim();
                topology = new Topology( String.format( "%s-%s", blueprint.getName(), environmentId ), environmentId,
                        subnet, null );
            }

            constructTopology( topology, placements );

            try
            {

                if ( grow )
                {
                    Environment environment = ( Environment ) envCombo.getValue();
                    environmentManager.growEnvironment( topology, true );

                    Notification.show( "Environment expanding started" );
                }
                else
                {
                    environmentManager.createEnvironment( topology, true );
                    Notification.show( "Environment creation started" );
                }

                close();
            }
            catch ( Exception e )
            {
                Notification.show( String.format( "Failed to %s environment: %s", grow ? "grow" : "create",
                        ExceptionUtils.getRootCauseMessage( e ) ), Notification.Type.ERROR_MESSAGE );
            }
        }
    }


    private void constructTopology( final Topology topology, final Set<NodeGroup> placements )
    {
        for ( NodeGroup placement : placements )
        {
            topology.addNodeGroupPlacement( peerManager.getPeer( placement.getPeerId() ), placement );
        }
    }


    private Set<NodeGroup> getPlacements()
    {
        Set<NodeGroup> placements = Sets.newHashSet();

        for ( Object itemId : placementTable.getItemIds() )
        {
            Item item = placementTable.getItem( itemId );
            String nodeGroupName = item.getItemProperty( "Name" ).getValue().toString();
            ResourceHostMetric metric = ( ResourceHostMetric ) item.getItemProperty( "Host" ).getValue();
            int amount = Integer.parseInt( item.getItemProperty( "Amount" ).getValue().toString() );

            for ( NodeGroup ng : blueprint.getNodeGroups() )
            {
                if ( ng.getName().equalsIgnoreCase( nodeGroupName ) )
                {
                    NodeGroup nodeGroup = new NodeGroup( nodeGroupName, ng.getTemplateName(), ng.getType(), amount,
                            ng.getSshGroupId(), ng.getHostsGroupId(), metric.getPeerId(), metric.getHostId() );

                    placements.add( nodeGroup );
                }
            }
        }

        return placements;
    }


    private Table createPlacementTable()
    {
        Table table = new Table();
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "Amount", Integer.class, null );
        table.addContainerProperty( "Host", ResourceHostMetric.class, null );
        table.addContainerProperty( "Action", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }


    private void populateNodeGroupsTable( final Table nodeGroupsTable )
    {
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
        {
            Slider slider = new Slider( 1, nodeGroup.getNumberOfContainers() );
            slider.setWidth( 100, Unit.PIXELS );
            slider.setOrientation( SliderOrientation.HORIZONTAL );
            slider.setValue( ( double ) nodeGroup.getNumberOfContainers() );

            ComboBox hostsComboBox = createHostsComboBox();
            hostsComboBox.setId( nodeGroup.getName() + "-hostsComboBox" );

            Button placeBtn = createPlaceButton( nodeGroup, slider, hostsComboBox );

            nodeGroupsTable.addItem( new Object[] {
                    nodeGroup.getName(), slider, hostsComboBox, placeBtn
            }, null );
        }
    }


    private Button createPlaceButton( final NodeGroup nodeGroup, final Slider slider, final ComboBox hostsComboBox )
    {
        Button placeButton = new Button( "Place" );
        placeButton.setId( "placeButton" );

        placeButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                if ( hostsComboBox.getValue() == null )
                {

                    Notification.show( "Please, select target host", Notification.Type.WARNING_MESSAGE );
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
                    placeNodeGroup( nodeGroup, slider, ( ResourceHostMetric ) hostsComboBox.getValue() );
                }
            }
        } );

        return placeButton;
    }


    private void placeNodeGroup( NodeGroup nodeGroup, final Slider slider, ResourceHostMetric metric )
    {
        final String rowId = String.format( "%s-%s", nodeGroup.getName(), metric.getHostId() );
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
                updateBuildButton();
            }
        } );

        final Item row = placementTable.getItem( rowId );

        if ( row == null )
        {
            placementTable.addItem( new Object[] {
                    nodeGroup.getName(), amount, metric, removeBtn
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

        updateBuildButton();
    }


    private void updateBuildButton()
    {
        buildBtn.setEnabled( !CollectionUtil.isCollectionEmpty( placementTable.getItemIds() ) );
    }


    private void initMetrics()
    {

        List<Peer> peers = peerManager.getPeers();

        for ( Peer peer : peers )
        {
            try
            {
                ResourceHostMetrics rm = peer.getResourceHostMetrics();
                for ( ResourceHostMetric m : rm.getResources() )
                {
                    metrics.add( m );
                }
            }
            catch ( Exception ignore )
            {
                // ignore
            }
        }
    }


    private ComboBox createHostsComboBox()
    {
        ComboBox hostCombo = new ComboBox();
        hostCombo.setNullSelectionAllowed( false );
        hostCombo.setTextInputAllowed( false );
        hostCombo.setImmediate( true );
        hostCombo.setRequired( true );

        for ( ResourceHostMetric metric : metrics )
        {
            hostCombo.addItem( metric );

            hostCombo.setItemCaption( metric, metric.getPeerId() + ":" + metric.getHostName() );
        }

        hostCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                ResourceHostMetric m = ( ResourceHostMetric ) event.getProperty().getValue();
                Notification.show( m.getHostName() );
            }
        } );

        return hostCombo;
    }


    private Table createNodeGroupsTable()
    {
        Table table = new Table();
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "Amount", Slider.class, null );
        table.addContainerProperty( "Host", ComboBox.class, null );
        table.addContainerProperty( "Action", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }
}
