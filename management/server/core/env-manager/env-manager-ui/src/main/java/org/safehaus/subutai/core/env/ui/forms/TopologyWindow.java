package org.safehaus.subutai.core.env.ui.forms;


import java.util.List;

import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.core.env.api.build.Blueprint;
import org.safehaus.subutai.core.env.api.build.NodeGroup;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.slider.SliderOrientation;
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


    public TopologyWindow( Blueprint blueprint, PeerManager peerManager )
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

        setContent( content );
    }


    private Table createPlacementTable()
    {
        Table table = new Table();
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "Amount", Integer.class, null );
        table.addContainerProperty( "Peer", String.class, null );
        table.addContainerProperty( "Remove", Button.class, null );
        table.addContainerProperty( "Build", Button.class, null );
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
            Slider slider = new Slider( 1, nodeGroup.getNumberOfNodes() );
            slider.setWidth( 100, Unit.PIXELS );
            slider.setOrientation( SliderOrientation.HORIZONTAL );
            slider.setValue( ( double ) nodeGroup.getNumberOfNodes() );

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
            }
        } );

        final Item row = placementTable.getItem( rowId );

        if ( row == null )
        {
            placementTable.addItem( new Object[] {
                    nodeGroup.getName(), amount, peer.getName(), removeBtn, null
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
