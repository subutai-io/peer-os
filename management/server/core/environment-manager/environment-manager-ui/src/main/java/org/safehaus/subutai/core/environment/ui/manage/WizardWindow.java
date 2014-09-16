package org.safehaus.subutai.core.environment.ui.manage;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.helper.BuildBlock;
import org.safehaus.subutai.core.environment.api.helper.BuildProcess;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;
import org.safehaus.subutai.core.environment.ui.window.DetailsWindow;
import org.safehaus.subutai.core.peer.api.Peer;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by bahadyr on 9/10/14.
 */
public class WizardWindow extends DetailsWindow {

    private static final Logger logger = Logger.getLogger( WizardWindow.class.getName() );

    int step = 0;
    EnvironmentBuildTask environmentBuildTask;
    Table peersTable;
    private EnvironmentManagerUI managerUI;


    public WizardWindow( final String caption, EnvironmentManagerUI managerUI,
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


    private VerticalLayout getTopologyForm() {
        VerticalLayout layout = new VerticalLayout();
        Panel panel = new Panel();
        panel.setContent( new Button( "test" ) );
        layout.addComponent( panel );


        Button next = new Button( "Next" );
        next.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                next();
            }
        } );


        layout.addComponent( next );

        return layout;
    }


    private VerticalLayout getPeersForm() {

        VerticalLayout layout = new VerticalLayout();
        Table peersTable = new Table();
        peersTable.setCaption( "Peers" );
        peersTable.setImmediate( false );
        peersTable.setWidth( "600px" );
        peersTable.setHeight( "400px" );


        List<Peer> peers = managerUI.getPeerManager().peers();

        BeanItemContainer<Peer> ds = new BeanItemContainer<Peer>( Peer.class );
        ds.addAll( peers );
        peersTable.setContainerDataSource( ds );

        Button back = new Button( "Next" );
        back.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                back();
            }
        } );

        layout.addComponent( back );

        Button next = new Button( "Next" );
        next.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                next();
            }
        } );


        layout.addComponent( next );
        layout.addComponent( peersTable );

        return layout;
    }


    public void back() {
        step--;
    }


    private VerticalLayout genPeersTable() {
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
        for ( Peer peer : peers )
        {
            CheckBox ch = new CheckBox();
            peersTable.addItem( new Object[] {
                    peer.getName(), ch
            }, null );
        }
        Button nextButton = new Button( "Next" );
        nextButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                next();
            }
        } );


        vl.addComponent( peersTable );
        vl.addComponent( nextButton );
        return vl;
    }


    private VerticalLayout genContainerToPeersTable() {


        VerticalLayout vl = new VerticalLayout();

        Table table = new Table();
        table.addContainerProperty( "Container", String.class, null );
        table.addContainerProperty( "Put", ComboBox.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();


        for ( NodeGroup ng : environmentBuildTask.getEnvironmentBlueprint().getNodeGroups() )
        {
            for ( int i = 0; i < ng.getNumberOfNodes(); i++ )
            {
                ComboBox box = new ComboBox( "", selectedPeers() );
                box.setNullSelectionAllowed( false );
                box.setTextInputAllowed( false );
                table.addItem( new Object[] {
                        ng.getTemplateName(), box
                }, null );
            }
        }
        Button nextButton = new Button( "Build" );
        nextButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                sendBuilProcessToBackground();
                close();
            }
        } );


        vl.addComponent( table );
        vl.addComponent( nextButton );
        return vl;
    }


    private List<String> selectedPeers() {
        List<String> l = new ArrayList<String>();
        l.add( "peer1" );
        l.add( "peer12" );
        l.add( "peer132" );
        l.add( "peer133" );
        l.add( "peer221" );
        return l;
    }


    private void sendBuilProcessToBackground() {
        BuildProcess buildProcess = new BuildProcess();
        for ( Object itemId : peersTable.getItemIds() )
        {
            String name = ( String ) peersTable.getItem( itemId ).getItemProperty( "Container" ).getValue();
            CheckBox selection = ( CheckBox ) peersTable.getItem( itemId ).getItemProperty( "Put" ).getValue();
            if ( selection.getValue() )
            {
                BuildBlock bb = new BuildBlock();
                bb.setPeerId( name );
                buildProcess.addBuildBlock( bb );
            }
        }

        managerUI.getEnvironmentManager().saveBuildProcess( buildProcess );
    }
}
