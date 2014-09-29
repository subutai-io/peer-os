package org.safehaus.subutai.core.environment.ui.manage;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.environment.ui.window.DetailsWindow;
import org.safehaus.subutai.core.peer.api.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 * Created by bahadyr on 9/10/14.
 */
public class EnvironmentBuildWizard extends DetailsWindow
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentBuildWizard.class.getName() );
    private int step = 0;
    private EnvironmentBuildTask environmentBuildTask;
    private Table peersTable;
    private Table containerToPeerTable;
    private EnvironmentManagerPortalModule managerUI;


    public EnvironmentBuildWizard( final String caption, EnvironmentManagerPortalModule managerUI,
                                   EnvironmentBuildTask environmentBuildTask )
    {
        super( caption );
        this.managerUI = managerUI;
        this.environmentBuildTask = environmentBuildTask;
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
                setContent( genPeersTable() );
                break;
            }
            case 2:
            {
                setContent( genContainerToPeersTable() );
                break;
            }
            case 3:
            {
                managerUI.getEnvironmentManager().buildEnvironment( environmentBuildTask );
                close();
                break;
            }
            default:
            {
                setContent( genPeersTable() );
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


    public EnvironmentBuildTask getEnvironmentBuildTask()
    {
        return environmentBuildTask;
    }


    public void setEnvironmentBuildTask( final EnvironmentBuildTask environmentBuildTask )
    {
        this.environmentBuildTask = environmentBuildTask;
    }


    public void back()
    {
        step--;
    }


    private VerticalLayout genPeersTable()
    {
        VerticalLayout vl = new VerticalLayout();

        peersTable = new Table();
        peersTable.addContainerProperty( "Name", String.class, null );
        peersTable.addContainerProperty( "Select", CheckBox.class, null );
        peersTable.setPageLength( 10 );
        peersTable.setSelectable( false );
        peersTable.setEnabled( true );
        peersTable.setImmediate( true );
        peersTable.setSizeFull();


        List<Peer> peers = managerUI.getPeerManager().peers();
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


    Map<Object, NodeGroup> map;


    private TabSheet genContainerToPeersTable()
    {

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();

        VerticalLayout vl = new VerticalLayout();

        containerToPeerTable = new Table();
        containerToPeerTable.addContainerProperty( "Container", String.class, null );
        containerToPeerTable.addContainerProperty( "Put", ComboBox.class, null );
        containerToPeerTable.setPageLength( 10 );
        containerToPeerTable.setSelectable( false );
        containerToPeerTable.setEnabled( true );
        containerToPeerTable.setImmediate( true );
        containerToPeerTable.setSizeFull();


        map = new HashMap<>();
        for ( NodeGroup ng : environmentBuildTask.getEnvironmentBlueprint().getNodeGroups() )
        {
            for ( int i = 0; i < ng.getNumberOfNodes(); i++ )
            {
                ComboBox comboBox = new ComboBox();
                BeanItemContainer<Peer> bic = new BeanItemContainer<>( Peer.class );
                bic.addAll( selectedPeers() );
                comboBox.setContainerDataSource( bic );
                comboBox.setNullSelectionAllowed( false );
                comboBox.setTextInputAllowed( false );
                comboBox.setItemCaptionPropertyId( "name" );
                Object itemId = containerToPeerTable.addItem( new Object[] {
                        ng.getTemplateName(), comboBox
                }, null );
                map.put( itemId, ng );
            }
        }
        Button nextButton = new Button( "Build" );
        nextButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                Map<Object, Peer> topology = topologySelection();
                if ( !topology.isEmpty() || containerToPeerTable.getItemIds().size() != topology.size() )
                {
                    managerUI.getEnvironmentManager().saveBuildProcess(
                            createBackgroundEnvironmentBuildProcess( environmentBuildTask, topology ) );
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
        sheet.addTab( vl, "Node to Peer" );
        sheet.addTab( new Button( "test" ), "Blueprint to Peer group" );
        sheet.addTab( new Button( "test" ), "Node group to Peer group" );
        sheet.addTab( new Button( "test" ), "Node group to Peer" );
        return sheet;
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


    public EnvironmentBuildProcess createBackgroundEnvironmentBuildProcess( EnvironmentBuildTask ebt,
                                                                            Map<Object, Peer> topology )
    {
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( ebt.getEnvironmentBlueprint().getName() );

        for ( Object itemId : map.keySet() )
        {
            Peer peer = topology.get( itemId );
            NodeGroup ng = map.get( itemId );

            String key = peer.getId().toString() + "-" + ng.getTemplateName();

            if ( !process.getMessageMap().containsKey( key ) )
            {
                CloneContainersMessage ccm = new CloneContainersMessage( process.getUuid(), peer.getId() );
                ccm.setTemplate( ng.getTemplateName() );
                ccm.setPeerId( peer.getId() );
                ccm.setEnvId( ebt.getUuid() );
                ccm.setNumberOfNodes( 1 );
                ccm.setStrategy( ng.getPlacementStrategy().toString() );
                process.putCloneContainerMessage( key, ccm );
            }
            else
            {
                process.getMessageMap().get( key ).incrementNumberOfNodes();
            }
        }

        return process;
    }


    private Map<Object, Peer> topologySelection()
    {
        Map<Object, Peer> topology = new HashMap<>();
        for ( Object itemId : containerToPeerTable.getItemIds() )
        {
            //            String templateName =
            //                    ( String ) containerToPeerTable.getItem( itemId ).getItemProperty( "Container" )
            // .getValue();
            ComboBox selection =
                    ( ComboBox ) containerToPeerTable.getItem( itemId ).getItemProperty( "Put" ).getValue();
            Peer peer = ( Peer ) selection.getValue();

            topology.put( itemId, peer );
        }
        return topology;
    }
}
