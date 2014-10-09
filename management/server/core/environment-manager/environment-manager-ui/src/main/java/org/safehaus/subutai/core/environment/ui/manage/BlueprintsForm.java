package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.environment.ui.window.BlueprintDetails;
import org.safehaus.subutai.core.peer.api.PeerGroup;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


@SuppressWarnings("serial")
public class BlueprintsForm
{

    private static final String NO_BLUEPRINTS = "No blueprints found";
    private static final String BLUEPRINT_2_PEERGROUP = "Blueprint 2 Peer Group";
    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerPortalModule module;
    private Button environmentsButton;

    public BlueprintsForm( EnvironmentManagerPortalModule module )
    {
        this.module = module;
        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        environmentsTable = createTable( "Blueprints", 300 );

        environmentsButton = new Button( "View" );
        environmentsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                updateTableData();
            }
        } );

        contentRoot.addComponent( environmentsButton );
        contentRoot.addComponent( environmentsTable );
    }


    private Table createTable( String caption, int size )
    {
        Table table = new Table( caption );
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( "View", Button.class, null );
        table.addContainerProperty( "Build", Button.class, null );
        table.addContainerProperty( "Delete", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }


    private void updateTableData()
    {
        environmentsTable.removeAllItems();
        List<EnvironmentBuildTask> tasks = module.getEnvironmentManager().getBlueprints();
        if ( !tasks.isEmpty() )
        {
            for ( final EnvironmentBuildTask task : tasks )
            {

                final Button viewButton = new Button( "View" );
                viewButton.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        BlueprintDetails details = new BlueprintDetails( "Blueprint details" );
                        details.setContent( task.getEnvironmentBlueprint() );
                        contentRoot.getUI().addWindow( details );
                        details.setVisible( true );
                    }
                } );

                final Button node2Peer = new Button( "Node2Peer" );
                node2Peer.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        EnvironmentBuildWizard environmentBuildWizard =
                                new EnvironmentBuildWizard( "Wizard", module, task );
                        contentRoot.getUI().addWindow( environmentBuildWizard );
                        environmentBuildWizard.setVisible( true );
                    }
                } );

                Button blueprint2PeerGroup = new Button( "Blueprint2PeerGroup" );
                blueprint2PeerGroup.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        showBlueprint2PeerGroupWindow();
                    }
                } );

                final Button deleteButton = new Button( "Delete" );
                deleteButton.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        module.getEnvironmentManager().deleteBlueprint( task.getUuid().toString() );
                        environmentsButton.click();
                    }
                } );

                environmentsTable.addItem( new Object[] {
                        task.getEnvironmentBlueprint().getName(), viewButton, node2Peer, deleteButton
                }, null );
            }
        }
        else
        {
            Notification.show( NO_BLUEPRINTS );
        }
        environmentsTable.refreshRowCache();
    }


    private void showBlueprint2PeerGroupWindow()
    {
        Window window = createWindow( BLUEPRINT_2_PEERGROUP );
        List<PeerGroup> list = module.getPeerManager().peersGroups();
    }

    private Window createWindow( String caption )
    {
        Window window = new Window();
        window.setCaption( caption );
        window.setModal( true );
        window.setClosable( true );
        return window;
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
