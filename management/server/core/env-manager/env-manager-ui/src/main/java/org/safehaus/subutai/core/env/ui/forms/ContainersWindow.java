package org.safehaus.subutai.core.env.ui.forms;


import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.env.api.Environment;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class ContainersWindow extends Window
{
    private final Environment environment;
    private Table containersTable;


    public ContainersWindow( final Environment environment )
    {
        this.environment = environment;

        setCaption( "Containers" );
        setWidth( "800px" );
        setHeight( "600px" );
        setModal( true );
        setClosable( true );

        VerticalLayout content = new VerticalLayout();
        content.setSpacing( true );
        content.setMargin( true );
        content.setStyleName( "default" );
        content.setSizeFull();

        containersTable = createContainersTable();

        updateContainersTable();

        content.addComponent( containersTable );

        setContent( content );
    }


    private void updateContainersTable()
    {

        for ( ContainerHost containerHost : environment.getContainerHosts() )
        {
            Button startBtn = new Button( "Start" );
            Button stopBtn = new Button( "Stop" );
            Button destroyBtn = new Button( "Destroy" );

            containersTable.addItem( new Object[] {
                    containerHost.getId().toString(), containerHost.getTemplateName(), containerHost.getHostname(),
                    containerHost.getIpByInterfaceName( "eth0" ), startBtn, stopBtn, destroyBtn
            }, null );
        }
    }


    private Table createContainersTable()
    {
        Table table = new Table();
        table.addContainerProperty( "Id", String.class, null );
        table.addContainerProperty( "Template", String.class, null );
        table.addContainerProperty( "Hostname", String.class, null );
        table.addContainerProperty( "IP", String.class, null );
        table.addContainerProperty( "Start", Button.class, null );
        table.addContainerProperty( "Stop", Button.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }
}
