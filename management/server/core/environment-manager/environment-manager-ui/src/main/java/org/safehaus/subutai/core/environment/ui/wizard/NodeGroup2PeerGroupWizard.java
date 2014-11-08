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
import org.safehaus.subutai.core.peer.api.PeerInfo;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * Created by bahadyr on 9/10/14.
 */
public class NodeGroup2PeerGroupWizard extends Window
{

    private int step = 0;
    private EnvironmentManagerPortalModule module;
    private EnvironmentBlueprint blueprint;


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
                setContent( generatePeerGroupsLayout() );
                break;
            }
            case 2:
            {
                setContent( generateNodeGroupLayout() );
                break;
            }
            default:
            {
                close();
                break;
            }
        }
    }


    private Table ngTopgTable;
    private Map<Object, NodeGroup> nodeGroupMap;


    private Component generateNodeGroupLayout()
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
            BeanItemContainer<PeerInfo> bic = new BeanItemContainer<>( PeerInfo.class );
            bic.addAll( selectedPeers() );
            comboBox.setContainerDataSource( bic );
            comboBox.setNullSelectionAllowed( false );
            comboBox.setTextInputAllowed( false );
            comboBox.setItemCaptionPropertyId( "name" );
            Object itemId = ngTopgTable.addItem( new Object[] {
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
                PeerGroup peerGroup = getSelectedPeerGroup();
                if ( peerGroup != null )
                {
                    NodeGroup2PeerGroupData data = new NodeGroup2PeerGroupData( blueprint.getId(), peerGroup.getId() );
                    try
                    {
                        module.getEnvironmentManager().saveBuildProcess( data );
                    }
                    catch ( EnvironmentManagerException e )
                    {
                        Notification.show( e.getMessage() );
                    }
                    next();
                }
                else
                {
                    Notification.show( "Please select peer group", Notification.Type.HUMANIZED_MESSAGE );
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
        for ( UUID uuid : peerGroup.getPeerIds() )
        {
            PeerInfo peer = module.getPeerManager().getPeerInfo( uuid );
            peerSet.add( peer );
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


    public void back()
    {
        step--;
    }


    final ComboBox peerGroupsCombo = new ComboBox();


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
                next();
            }
        } );


        vl.addComponent( peerGroupsCombo );
        vl.addComponent( nextButton );
        return vl;
    }


    private PeerGroup getSelectedPeerGroup()
    {
        return ( PeerGroup ) peerGroupsCombo.getValue();
    }
}
