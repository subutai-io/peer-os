package org.safehaus.subutai.core.environment.ui.wizard;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.topology.NodeGroup2PeerData;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.peer.api.Peer;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class NodeGroup2PeerWizard extends Window
{

    private int step = 0;
    private Table peersTable;
    private Table ngTopgTable;
    private EnvironmentManagerPortalModule managerUI;
    private Map<Integer, NodeGroup> nodeGroupMap;
    private EnvironmentBlueprint blueprint;


    public NodeGroup2PeerWizard( final String caption, EnvironmentManagerPortalModule managerUI,
                                 EnvironmentBlueprint blueprint )
    {
        super( caption );
        setCaption( caption );
        setModal( true );
        setClosable( true );
        setVisible( false );
        setWidth( "800px" );
        setHeight( "500px" );
        this.managerUI = managerUI;
        this.blueprint = blueprint;
        next();
    }


    private void next()
    {
        step++;
        putForm();
    }


    private void putForm()
    {
        switch ( step )
        {
            case 1:
                setContent( genPeersTable() );
                break;
            case 2:
                setContent( genNodeGroupToPeersTable() );
                break;
            default:
                setContent( genPeersTable() );
                break;
        }
    }


    public EnvironmentManagerPortalModule getManagerUI()
    {
        return managerUI;
    }


    public void setManagerUI( final EnvironmentManagerPortalModule managerUI )
    {
        this.managerUI = managerUI;
    }


    private VerticalLayout genPeersTable()
    {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin( true );

        peersTable = new Table();
        peersTable.addContainerProperty( "Name", String.class, null );
        peersTable.addContainerProperty( "Select", CheckBox.class, null );
        peersTable.setPageLength( 10 );
        peersTable.setSelectable( false );
        peersTable.setEnabled( true );
        peersTable.setImmediate( true );
        peersTable.setSizeFull();


        List<Peer> peers = managerUI.getPeerManager().getPeers();
        if ( !peers.isEmpty() )
        {
            for ( Peer peer : peers )
            {
                CheckBox checkBox = new CheckBox();
                peersTable.addItem( new Object[]
                {
                    peer.getName(), checkBox
                }, peer );
            }
        }
        Button nextButton = new Button( "Next" );
        nextButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                if ( !selectedPeers().isEmpty() )
                {
                    next();
                }
                else
                {
                    Notification.show( "Please select peers", Notification.Type.HUMANIZED_MESSAGE );
                }
            }
        } );


        vl.addComponent( peersTable );
        vl.addComponent( nextButton );
        return vl;
    }


    private VerticalLayout genNodeGroupToPeersTable()
    {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin( true );

        ngTopgTable = new Table();
        ngTopgTable.addContainerProperty( "Node Group", String.class, null );
        ngTopgTable.addContainerProperty( "Put", ComboBox.class, null );
        ngTopgTable.setPageLength( 10 );
        ngTopgTable.setSelectable( false );
        ngTopgTable.setEnabled( true );
        ngTopgTable.setImmediate( true );
        ngTopgTable.setSizeFull();
        nodeGroupMap = new HashMap<>();
        for ( NodeGroup ng : blueprint.getNodeGroups() )
        {
            ComboBox comboBox = new ComboBox();
            BeanItemContainer<Peer> bic = new BeanItemContainer<>( Peer.class );
            bic.addAll( selectedPeers() );
            comboBox.setContainerDataSource( bic );
            comboBox.setNullSelectionAllowed( false );
            comboBox.setTextInputAllowed( false );
            comboBox.setItemCaptionPropertyId( "name" );
            Integer itemId = ( Integer ) ngTopgTable.addItem( new Object[]
            {
                ng.getName(), comboBox
            }, null );
            nodeGroupMap.put( itemId, ng );
        }

        Button nextButton = new Button( "Save build task" );
        nextButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                Map<Integer, Peer> topology = topologySelection();
                if ( !topology.isEmpty() || ngTopgTable.getItemIds().size() != topology.size() )
                {
                    try
                    {
                        managerUI.getEnvironmentManager()
                                .saveBuildProcess( new NodeGroup2PeerData( blueprint.getId(), topology, nodeGroupMap ) );
                    }
                    catch ( EnvironmentManagerException e )
                    {
                        Notification.show( e.getMessage() );
                    }
                }
                else
                {
                    Notification.show( "Topology is not properly set" );
                }
                close();
            }
        } );


        vl.addComponent( ngTopgTable );
        vl.addComponent( nextButton );

        return vl;
    }


    private List<Peer> selectedPeers()
    {
        List<Peer> peers = new ArrayList<>();
        for ( Object itemId : peersTable.getItemIds() )
        {
            CheckBox selection = ( CheckBox ) peersTable.getItem( itemId ).getItemProperty( "Select" ).getValue();
            if ( selection.getValue() )
            {
                peers.add( ( Peer ) itemId );
            }
        }
        return peers;
    }


    public Map<Integer, Peer> topologySelection()
    {
        Map<Integer, Peer> topology = new HashMap<>();
        for ( Object itemId : ngTopgTable.getItemIds() )
        {
            ComboBox selection = ( ComboBox ) ngTopgTable.getItem( ( Integer ) itemId ).getItemProperty(
                    "Put" ).getValue();
            Peer peer = ( Peer ) selection.getValue();

            topology.put( ( Integer ) itemId, peer );
        }
        return topology;
    }
}

