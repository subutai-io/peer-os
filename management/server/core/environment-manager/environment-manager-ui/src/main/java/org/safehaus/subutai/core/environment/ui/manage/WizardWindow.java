package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;
import org.safehaus.subutai.core.environment.ui.window.DetailsWindow;
import org.safehaus.subutai.core.peer.api.Peer;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by bahadyr on 9/10/14.
 */
public class WizardWindow extends DetailsWindow {

    int step = 0;
    EnvironmentBuildTask environmentBuildTask;
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
                setContent( genPeers2Table() );
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

        Table table = new Table();
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "Select", CheckBox.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();


        List<Peer> peers = managerUI.getPeerManager().peers();
        for ( Peer peer : peers )
        {
            table.addItem( new Object[] {
                    peer.getName(), new CheckBox()
            }, null );
        }
        Button nextButton = new Button( "Next" );
        nextButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                next();
            }
        } );


        vl.addComponent( table );
        vl.addComponent( nextButton );
        return vl;
    }


    private VerticalLayout genPeers2Table() {
        VerticalLayout vl = new VerticalLayout();

        Table table = new Table();
        table.addContainerProperty( "Na123213me", String.class, null );
        table.addContainerProperty( "Sel23423ect", CheckBox.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();


        List<Peer> peers = managerUI.getPeerManager().peers();
        for ( Peer peer : peers )
        {
            table.addItem( new Object[] {
                    peer.getName(), new CheckBox()
            }, null );
        }
        Button nextButton = new Button( "Finish" );
        nextButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                close();
            }
        } );


        vl.addComponent( table );
        vl.addComponent( nextButton );
        return vl;
    }
}
