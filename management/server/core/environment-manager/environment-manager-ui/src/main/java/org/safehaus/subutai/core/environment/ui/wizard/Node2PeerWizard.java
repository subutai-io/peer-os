package org.safehaus.subutai.core.environment.ui.wizard;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.topology.Node2PeerData;
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


/**
 * Created by bahadyr on 9/10/14.
 */
public class Node2PeerWizard extends Window
{

    private int step = 0;
    private EnvironmentBlueprint blueprint;
    private Table peersTable;
    private Table containerToPeerTable;
    private EnvironmentManagerPortalModule managerUI;
    private Map<Object, NodeGroup> nodeGroupMap;


    public Node2PeerWizard( final String caption, EnvironmentManagerPortalModule managerUI,
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


    public void next()
    {
        step++;
        putForm();
    }


    private void putForm()
    {
        switch ( step )
        {
            case 1:
            {
                setContent( genPeersLayout() );
                break;
            }
            case 2:
            {
                setContent( genNodesToPeersLayout() );
                break;
            }
            default:
            {
                setContent( genPeersLayout() );
                break;
            }
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


    public void back()
    {
        step--;
    }


    private VerticalLayout genPeersLayout()
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
                peersTable.addItem( new Object[] {
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


    private VerticalLayout genNodesToPeersLayout()
    {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin( true );

        containerToPeerTable = new Table();
        containerToPeerTable.addContainerProperty( "Container", String.class, null );
        containerToPeerTable.addContainerProperty( "Put", ComboBox.class, null );
        containerToPeerTable.setPageLength( 10 );
        containerToPeerTable.setSelectable( false );
        containerToPeerTable.setEnabled( true );
        containerToPeerTable.setImmediate( true );
        containerToPeerTable.setSizeFull();
        nodeGroupMap = new HashMap<>();
        for ( NodeGroup ng : blueprint.getNodeGroups() )
        {
            for ( int i = 0; i < ng.getNumberOfNodes(); i++ )
            {
                ComboBox peersBox = new ComboBox();
                BeanItemContainer<Peer> bic = new BeanItemContainer<>( Peer.class );
                bic.addAll( selectedPeers() );
                peersBox.setContainerDataSource( bic );
                peersBox.setNullSelectionAllowed( false );
                peersBox.setTextInputAllowed( false );
                peersBox.setItemCaptionPropertyId( "name" );
                Object itemId = containerToPeerTable.addItem( new Object[] {
                        ng.getTemplateName(), peersBox
                }, null );
                nodeGroupMap.put( itemId, ng );
            }
        }
        Button nextButton = new Button( "Save build task" );
        nextButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                Map<Object, Peer> topology = topologySelection();
                if ( !topology.isEmpty() || containerToPeerTable.getItemIds().size() != topology.size() )
                {
                    Map<Object, NodeGroup> map = getNodeGroupMap();
                    Node2PeerData data = new Node2PeerData( blueprint.getId(), topology, map );
                    try
                    {
                        UUID processId = managerUI.getEnvironmentManager().saveBuildProcess( data );
                        Notification.show( String.format( "Process %s prepared", processId.toString() ) );
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


        vl.addComponent( containerToPeerTable );
        vl.addComponent( nextButton );

        return vl;
    }


    private List<Peer> selectedPeers()
    {
        List<Peer> peers = new ArrayList<>();
        for ( Object itemId : getPeersTable().getItemIds() )
        {
            CheckBox selection = ( CheckBox ) getPeersTable().getItem( itemId ).getItemProperty( "Select" ).getValue();
            if ( selection.getValue() )
            {
                peers.add( ( Peer ) itemId );
            }
        }
        return peers;
    }


    public Table getPeersTable()
    {
        return peersTable;
    }


    public Table getContainerToPeerTable()
    {
        return containerToPeerTable;
    }


    public Map<Object, NodeGroup> getNodeGroupMap()
    {
        return nodeGroupMap;
    }


    public void setNodeGroupMap( final Map<Object, NodeGroup> nodeGroupMap )
    {
        this.nodeGroupMap = nodeGroupMap;
    }


    public Map<Object, Peer> topologySelection()
    {
        Map<Object, Peer> topology = new HashMap<>();
        for ( Object itemId : getContainerToPeerTable().getItemIds() )
        {
            ComboBox selection =
                    ( ComboBox ) getContainerToPeerTable().getItem( itemId ).getItemProperty( "Put" ).getValue();
            Peer peer = ( Peer ) selection.getValue();

            topology.put( itemId, peer );
        }
        return topology;
    }
}
