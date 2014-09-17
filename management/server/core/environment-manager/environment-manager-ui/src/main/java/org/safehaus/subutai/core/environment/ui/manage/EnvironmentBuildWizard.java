package org.safehaus.subutai.core.environment.ui.manage;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.helper.ContainerBuildMessage;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;
import org.safehaus.subutai.core.environment.ui.window.DetailsWindow;
import org.safehaus.subutai.core.peer.api.Peer;

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
public class EnvironmentBuildWizard extends DetailsWindow {

    private static final Logger LOG = Logger.getLogger( EnvironmentBuildWizard.class.getName() );

    int step = 0;
    EnvironmentBuildTask environmentBuildTask;
    Table peersTable;
    Table containerToPeerTable;
    private EnvironmentManagerUI managerUI;


    public EnvironmentBuildWizard( final String caption, EnvironmentManagerUI managerUI,
                                   EnvironmentBuildTask environmentBuildTask ) {
        super( caption );
        this.managerUI = managerUI;
        this.environmentBuildTask = environmentBuildTask;
        next();
    }


    public void next() {
        step++;
        putForm();
    }


    private void putForm() {
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
        }
    }


    public EnvironmentManagerUI getManagerUI() {
        return managerUI;
    }


    public void setManagerUI( final EnvironmentManagerUI managerUI ) {
        this.managerUI = managerUI;
    }


    public EnvironmentBuildTask getEnvironmentBuildTask() {
        return environmentBuildTask;
    }


    public void setEnvironmentBuildTask( final EnvironmentBuildTask environmentBuildTask ) {
        this.environmentBuildTask = environmentBuildTask;
    }


    public void back() {
        step--;
    }


    private VerticalLayout genPeersTable() {
        VerticalLayout vl = new VerticalLayout();

        peersTable = new Table();
        peersTable.addContainerProperty( "Name", String.class, null );
        peersTable.addContainerProperty( "ID", UUID.class, null );
        peersTable.addContainerProperty( "Select", CheckBox.class, null );
        peersTable.setPageLength( 10 );
        peersTable.setSelectable( false );
        peersTable.setEnabled( true );
        peersTable.setImmediate( true );
        peersTable.setSizeFull();


        List<Peer> peers = managerUI.getPeerManager().peers();
        for ( Peer peer : peers )
        {
            CheckBox ch = new CheckBox();

            Object id = peersTable.addItem( new Object[] {
                    peer.getName(), peer.getIp(), ch
            }, null );
            peersTable.setItemCaptionPropertyId( "name" );
        }
        Button nextButton = new Button( "Next" );
        nextButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                if ( selectedPeers().size() > 0 )
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


    private TabSheet genContainerToPeersTable() {

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


        for ( NodeGroup ng : environmentBuildTask.getEnvironmentBlueprint().getNodeGroups() )
        {
            for ( int i = 0; i < ng.getNumberOfNodes(); i++ )
            {
                ComboBox box = new ComboBox( "", selectedPeers() );
                box.setNullSelectionAllowed( false );
                box.setTextInputAllowed( false );
                containerToPeerTable.addItem( new Object[] {
                        ng.getTemplateName(), box
                }, null );
            }
        }
        Button nextButton = new Button( "Build" );
        nextButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                createBackgroundEnvironmentBuildProcess();
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


    private List<String> selectedPeers() {
        List<String> l = new ArrayList<String>();
        for ( Object itemId : peersTable.getItemIds() )
        {
            String name = ( String ) peersTable.getItem( itemId ).getItemProperty( "Name" ).getValue();
            CheckBox selection = ( CheckBox ) peersTable.getItem( itemId ).getItemProperty( "Select" ).getValue();
            if ( selection.getValue() )
            {
                l.add( name );
            }
        }
        return l;
    }


    private void createBackgroundEnvironmentBuildProcess() {
        EnvironmentBuildProcess environmentBuildProcess = new EnvironmentBuildProcess();

        Map<String, Map<String, ContainerBuildMessage>> buildMessageMap =
                new HashMap<String, Map<String, ContainerBuildMessage>>();

        for ( Object itemId : containerToPeerTable.getItemIds() )
        {
            String templateName =
                    ( String ) containerToPeerTable.getItem( itemId ).getItemProperty( "Container" ).getValue();
            ComboBox selection =
                    ( ComboBox ) containerToPeerTable.getItem( itemId ).getItemProperty( "Put" ).getValue();

            String peerName = ( String ) selection.getValue();


            if ( !buildMessageMap.containsKey( peerName ) )
            {
                if ( !buildMessageMap.get( peerName ).containsKey( templateName ) )
                {
                    ContainerBuildMessage cbm = new ContainerBuildMessage();
                    cbm.setTemplateName( templateName );
                    cbm.setPeerId( peerName );
                    cbm.setCompleteState( false );
                    cbm.setEnvironmentUuid( environmentBuildTask.getUuid().toString() );
                    buildMessageMap.get( peerName ).put( templateName, cbm );
                }
                else
                {
                    buildMessageMap.get( peerName ).get( templateName ).incrementNumOfCont();
                }

                //                buildMessageMapput( peerName, buildBlock );
            }
            else
            {
                //                buildMessageMap.get( templateName ).get
            }
        }

        managerUI.getEnvironmentManager().saveBuildProcess( environmentBuildProcess );
    }
}
