package org.safehaus.subutai.core.environment.ui.manage;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;
import org.safehaus.subutai.core.environment.ui.window.DetailsWindow;
import org.safehaus.subutai.core.peer.api.Peer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    private static final Logger LOG = Logger.getLogger( EnvironmentBuildWizard.class.getName() );

    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    int step = 0;
    EnvironmentBuildTask environmentBuildTask;
    Table peersTable;
    Table containerToPeerTable;
    private EnvironmentManagerUI managerUI;


    public EnvironmentBuildWizard( final String caption, EnvironmentManagerUI managerUI,
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
        }
    }


    public EnvironmentManagerUI getManagerUI()
    {
        return managerUI;
    }


    public void setManagerUI( final EnvironmentManagerUI managerUI )
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
                    peer.getName(), peer.getId(), ch
            }, null );
            peersTable.setItemCaptionPropertyId( "name" );
        }
        Button nextButton = new Button( "Next" );
        nextButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
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


        for ( NodeGroup ng : environmentBuildTask.getEnvironmentBlueprint().getNodeGroups() )
        {
            for ( int i = 0; i < ng.getNumberOfNodes(); i++ )
            {
                ComboBox comboBox = new ComboBox( "", selectedPeers() );
                comboBox.setNullSelectionAllowed( false );
                comboBox.setTextInputAllowed( false );
                containerToPeerTable.addItem( new Object[] {
                        ng.getTemplateName(), comboBox
                }, null );
            }
        }
        Button nextButton = new Button( "Build" );
        nextButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
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


    private List<UUID> selectedPeers()
    {
        List<UUID> uuids = new ArrayList<>();
        for ( Object itemId : peersTable.getItemIds() )
        {
            UUID uuid = ( UUID ) peersTable.getItem( itemId ).getItemProperty( "ID" ).getValue();
            CheckBox selection = ( CheckBox ) peersTable.getItem( itemId ).getItemProperty( "Select" ).getValue();
            if ( selection.getValue() )
            {
                uuids.add( uuid );
            }
        }
        return uuids;
    }


    private void createBackgroundEnvironmentBuildProcess()
    {
        EnvironmentBuildProcess process = new EnvironmentBuildProcess();


        for ( Object itemId : containerToPeerTable.getItemIds() )
        {
            String templateName =
                    ( String ) containerToPeerTable.getItem( itemId ).getItemProperty( "Container" ).getValue();
            ComboBox selection =
                    ( ComboBox ) containerToPeerTable.getItem( itemId ).getItemProperty( "Put" ).getValue();
            UUID peerUuid = ( UUID ) selection.getValue();


            CloneContainersMessage ccm = new CloneContainersMessage();
            ccm.setTemplate( templateName );
            ccm.setPeerId( peerUuid );
            ccm.setEnvId( environmentBuildTask.getUuid() );
            ccm.setNumberOfNodes( 2 );
            process.getCloneContainersMessages().add( ccm );
        }


        managerUI.getEnvironmentManager().saveBuildProcess( process );
    }
}
