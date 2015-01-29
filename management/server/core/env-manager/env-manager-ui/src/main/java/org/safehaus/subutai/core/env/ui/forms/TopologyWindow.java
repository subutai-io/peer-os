package org.safehaus.subutai.core.env.ui.forms;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.env.api.Environment;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.common.environment.Blueprint;
import org.safehaus.subutai.common.environment.NodeGroup;
import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentModificationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentNotFoundException;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.common.collect.Maps;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class TopologyWindow extends Window
{
    private final Blueprint blueprint;
    private final PeerManager peerManager;
    private Table placementTable;
    private Button buildBtn;
    private ComboBox envCombo;


    public TopologyWindow( final Blueprint blueprint, final PeerManager peerManager,
                           final EnvironmentManager environmentManager, final boolean grow )
    {

        this.blueprint = blueprint;
        this.peerManager = peerManager;

        setCaption( "Topology" );
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

        buildBtn = new Button( grow ? "Grow" : "Build" );
        buildBtn.setEnabled( false );
        buildBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                Map<Peer, Set<NodeGroup>> placements = getPlacements();

                if ( placements == null )
                {
                    Notification.show( "Failed to obtain topology", Notification.Type.ERROR_MESSAGE );
                }
                else if ( grow && envCombo.getValue() == null )
                {
                    Notification.show( "Please, select environment to grow" );
                }
                else
                {

                    Topology topology = new Topology();

                    for ( Map.Entry<Peer, Set<NodeGroup>> placement : placements.entrySet() )
                    {
                        for ( NodeGroup nodeGroup : placement.getValue() )
                        {
                            topology.addNodeGroupPlacement( placement.getKey(), nodeGroup );
                        }
                    }

                    try
                    {
                        if ( grow )
                        {
                            Environment environment = ( Environment ) envCombo.getValue();
                            environmentManager.growEnvironment( environment.getId(), topology, true );
                            Notification.show( "Environment growth started" );
                        }
                        else
                        {
                            environmentManager.createEnvironment(
                                    String.format( "%s-%s", blueprint.getName(), UUID.randomUUID() ), topology, true );
                            Notification.show( "Environment creation started" );
                        }
                    }
                    catch ( EnvironmentModificationException | EnvironmentNotFoundException |
                            EnvironmentCreationException e )
                    {
                        Notification.show( String.format( "Failed to %s environment: %s", grow ? "grow" : "create", e ),
                                Notification.Type.ERROR_MESSAGE );
                    }


                    close();
                }
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
        content.addComponent( buildBtn );
        content.setComponentAlignment( buildBtn, Alignment.TOP_RIGHT );

        setContent( content );
    }


    private Map<Peer, Set<NodeGroup>> getPlacements()
    {
        Map<Peer, Set<NodeGroup>> placements = Maps.newHashMap();

        for ( Object itemId : placementTable.getItemIds() )
        {
            Item item = placementTable.getItem( itemId );
            String nodeGroupName = item.getItemProperty( "Name" ).getValue().toString();
            String peerName = item.getItemProperty( "Peer" ).getValue().toString();
            int amount = Integer.parseInt( item.getItemProperty( "Amount" ).getValue().toString() );

            NodeGroup nodeGroup = null;
            for ( NodeGroup ng : blueprint.getNodeGroups() )
            {
                if ( ng.getName().equalsIgnoreCase( nodeGroupName ) )
                {
                    nodeGroup = new NodeGroup( nodeGroupName, ng.getTemplateName(), ng.getDomainName(), amount,
                            ng.getSshGroupId(), ng.getHostsGroupId(), ng.getContainerPlacementStrategy() );
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
        Table table = new Table();
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "Amount", Integer.class, null );
        table.addContainerProperty( "Peer", String.class, null );
        table.addContainerProperty( "Remove", Button.class, null );
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

            ComboBox peersCombo = createPeersComboBox();

            Button placeBtn = createPlaceButton( nodeGroup, slider, peersCombo );

            nodeGroupsTable.addItem( new Object[] {
                    nodeGroup.getName(), slider, peersCombo, placeBtn
            }, null );
        }
    }


    private Button createPlaceButton( final NodeGroup nodeGroup, final Slider slider, final ComboBox peersCombo )
    {
        Button placeButton = new Button( "Place" );

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
                    placeNodeGroup( nodeGroup, slider, ( Peer ) peersCombo.getValue() );
                }
            }
        } );

        return placeButton;
    }


    private void placeNodeGroup( NodeGroup nodeGroup, final Slider slider, Peer peer )
    {
        final String rowId = String.format( "%s-%s", nodeGroup.getName(), peer.getId() );
        Button removeBtn = new Button( "Remove" );


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
                    nodeGroup.getName(), amount, peer.getName(), removeBtn
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
            peersCombo.addItem( peer );

            peersCombo.setItemCaption( peer, peer.getName() );
        }

        peersCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                Peer peer = ( Peer ) event.getProperty().getValue();
                Notification.show( peer.getName() );
            }
        } );

        return peersCombo;
    }


    private Table createNodeGroupsTable()
    {
        Table table = new Table();
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "Amount", Slider.class, null );
        table.addContainerProperty( "Peer", ComboBox.class, null );
        table.addContainerProperty( "Place", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }
}
