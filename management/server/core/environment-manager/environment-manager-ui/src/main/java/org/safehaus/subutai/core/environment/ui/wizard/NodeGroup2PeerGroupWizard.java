package org.safehaus.subutai.core.environment.ui.wizard;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.topology.NodeGroup2PeerGroupData;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.peer.api.PeerGroup;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class NodeGroup2PeerGroupWizard extends Window
{
    public static final String PROPERTY_NODE_GROUP = "Node Group";
    public static final String PROPERTY_PEER = "Put";

    private static final Logger LOGGER = LoggerFactory.getLogger( NodeGroup2PeerGroupWizard.class );

    private int step = 0;
    private EnvironmentManagerPortalModule module;
    private EnvironmentBlueprint blueprint;

    private Table ngTopgTable;
    final ComboBox peerGroupsCombo = new ComboBox();


    public NodeGroup2PeerGroupWizard( final String caption, EnvironmentManagerPortalModule module,
                                      EnvironmentBlueprint blueprint )
    {
        super( caption );
        setCaption( caption );
        setModal( true );
        setClosable( true );
        setVisible( false );
        setWidth( "800px" );
        setHeight( "500px" );

        this.module = module;
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
                setContent( generatePeerGroupsLayout() );
                break;
            case 2:
                setContent( generateNodeGroupLayout() );
                break;
            default:
                close();
                break;
        }
    }


    private Component generateNodeGroupLayout()
    {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin( true );

        ngTopgTable = new Table();
        ngTopgTable.addContainerProperty( PROPERTY_NODE_GROUP, String.class, null );
        ngTopgTable.addContainerProperty( PROPERTY_PEER, ComboBox.class, null );
        ngTopgTable.setPageLength( 10 );
        ngTopgTable.setSelectable( false );
        ngTopgTable.setEnabled( true );
        ngTopgTable.setImmediate( true );
        ngTopgTable.setSizeFull();
        for ( NodeGroup ng : blueprint.getNodeGroups() )
        {
            BeanItemContainer<PeerInfo> bic = new BeanItemContainer<>( PeerInfo.class );
            bic.addAll( selectedPeers() );
            ComboBox comboBox = new ComboBox();
            comboBox.setContainerDataSource( bic );
            comboBox.setNullSelectionAllowed( false );
            comboBox.setTextInputAllowed( false );
            comboBox.setItemCaptionPropertyId( "name" );
            ngTopgTable.addItem( new Object[]
            {
                ng.getName(), comboBox
            }, null );
        }

        Button nextButton = new Button( "Save build task" );
        nextButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                Map<NodeGroup, UUID> matches = getMatches();
                if ( matches != null && matches.size() == blueprint.getNodeGroups().size() )
                {
                    PeerGroup pg = getSelectedPeerGroup();
                    NodeGroup2PeerGroupData data = new NodeGroup2PeerGroupData( blueprint.getId(), pg.getId() );
                    data.setNodeGroupToPeer( matches );
                    try
                    {
                        module.getEnvironmentManager().saveBuildProcess( data );
                    }
                    catch ( EnvironmentManagerException e )
                    {
                        Notification.show( e.getMessage() );
                    }
                    catch ( Exception ex )
                    {
                        LOGGER.error( "Failed to save", ex );
                    }
                    next();
                }
                else
                {
                    Notification.show( "Invalid match of node groups", Notification.Type.HUMANIZED_MESSAGE );
                }
            }
        } );


        vl.addComponent( ngTopgTable );
        vl.addComponent( nextButton );
        return vl;
    }


    private Set<PeerInfo> selectedPeers()
    {
        Set<PeerInfo> peerSet = new HashSet<>();
        PeerGroup peerGroup = getSelectedPeerGroup();
        if ( peerGroup != null )
        {
            for ( UUID uuid : peerGroup.getPeerIds() )
            {
                PeerInfo peer = module.getPeerManager().getPeerInfo( uuid );
                peerSet.add( peer );
            }
        }
        return peerSet;
    }


    public EnvironmentManagerPortalModule getModule()
    {
        return module;
    }


    public void setModule( final EnvironmentManagerPortalModule module )
    {
        this.module = module;
    }


    private VerticalLayout generatePeerGroupsLayout()
    {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin( true );

        List<PeerGroup> peerGroups = module.getPeerManager().peersGroups();

        BeanItemContainer<PeerGroup> bic = new BeanItemContainer<>( PeerGroup.class );
        bic.addAll( peerGroups );

        peerGroupsCombo.setContainerDataSource( bic );
        peerGroupsCombo.setNullSelectionAllowed( false );
        peerGroupsCombo.setTextInputAllowed( false );
        peerGroupsCombo.setItemCaptionPropertyId( "name" );

        Button nextButton = new Button( "Next" );
        nextButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                if ( getSelectedPeerGroup() != null )
                {
                    next();
                }
                else
                {
                    Notification.show( "Select peer group" );
                }
            }
        } );


        vl.addComponent( peerGroupsCombo );
        vl.addComponent( nextButton );
        return vl;
    }


    private PeerGroup getSelectedPeerGroup()
    {
        Object selected = peerGroupsCombo.getValue();
        return selected instanceof PeerGroup ? ( PeerGroup ) selected : null;
    }


    private Map<NodeGroup, UUID> getMatches()
    {
        Map<NodeGroup, UUID> result = new HashMap<>();
        for ( Object itemId : ngTopgTable.getItemIds() )
        {
            Item item = ngTopgTable.getItem( itemId );
            String ngName = item.getItemProperty( PROPERTY_NODE_GROUP ).getValue().toString();
            NodeGroup ng = getNodeGroupByName( ngName );
            if ( ng != null )
            {
                Object cmb = item.getItemProperty( PROPERTY_PEER ).getValue();
                if ( cmb instanceof ComboBox )
                {
                    Object selected = ( ( ComboBox ) cmb ).getValue();
                    if ( selected instanceof PeerInfo )
                    {
                        result.put( ng, ( ( PeerInfo ) selected ).getId() );
                    }
                }
            }
        }
        return result;
    }


    private NodeGroup getNodeGroupByName( String name )
    {
        for ( NodeGroup ng : blueprint.getNodeGroups() )
        {
            if ( ng.getName().equals( name ) )
            {
                return ng;
            }
        }
        return null;
    }
}

