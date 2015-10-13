import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.service.ContainerInfo;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.tracker.api.Tracker;


public class NodeRegistrationComponent extends CustomComponent
{
    protected static final String LIST_BUTTON = "List requests";

    private RegistrationManager registrationManager;
    private ExecutorService executorService;
    private Tracker tracker;
    private GridLayout gridLayout;
    private Table table;
    private ScheduledExecutorService updater;


    public NodeRegistrationComponent( final ExecutorService executerService, NodeRegistrationPortalModule managerUi,
                                      RegistrationManager registrationManager, Tracker tracker )
    {
        this.executorService = executerService;
        this.tracker = tracker;
        this.registrationManager = registrationManager;

        gridLayout = new GridLayout();
        gridLayout.setColumns( 1 );
        gridLayout.setRows( 20 );
        gridLayout.setSpacing( true );
        gridLayout.setMargin( true );
        gridLayout.setSizeFull();

        table = new Table( "Imported containers" );
        table.setId( "ContainersTable" );

        final HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );
    }


    private Table drawTable( String name, Set<ContainerInfo> containers )
    {
        final Table table = new Table( name );
        table.setId( name );

        table.setStyleName( "Reindeer.TABLE_STRONG" );
        table.setColumnCollapsingAllowed( true );
        table.addContainerProperty( TableAttributes.CONTAINER_NAME, String.class, null );
        table.addContainerProperty( TableAttributes.IP, String.class, null );
        table.addContainerProperty( TableAttributes.TEMPLATE, String.class, null );
        table.setColumnCollapsed( TableAttributes.TEMPLATE, true );
        table.addContainerProperty( TableAttributes.VLAN, String.class, null );
        table.addContainerProperty( TableAttributes.GPG_PK, String.class, null );
        table.setColumnCollapsed( TableAttributes.GPG_PK, true );
        table.addContainerProperty( TableAttributes.AVAILABLE_OPERATIONS, String.class, null );

        return table;
    }


    private void listRequests()
    {

        List<RequestedHost> requestedHosts = registrationManager.getRequests();

        for ( final RequestedHost requestedHost : requestedHosts )
        {
            VerticalLayout layout = new VerticalLayout();

            String rhInfo = String.format( "ID:%s \n Hostname:%s \n Secret:%s", requestedHost.getId(),
                    requestedHost.getHostInfos(), requestedHost.getSecret() );

            Label label = new Label( rhInfo );
            label.setId( requestedHost.getId() );
            layout.addComponent( label );
            gridLayout.addComponent( layout );

            Table contTable = drawTable( requestedHost.getHostname(), requestedHost.getHostInfos() );

            gridLayout.addComponent( contTable );
        }
    }


    private class TableAttributes
    {
        protected static final String AVAILABLE_OPERATIONS = "Available actions";
        protected static final String GPG_PK = "GPG PK";
        protected static final String CONTAINER_NAME = "Container name";
        protected static final String VLAN = "VLAN";
        protected static final String IP = "IP";
        protected static final String TEMPLATE = "Template";
    }
}
