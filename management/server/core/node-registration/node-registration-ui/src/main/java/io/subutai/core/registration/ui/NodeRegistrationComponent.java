package io.subutai.core.registration.ui;


import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.vaadin.ui.Button;
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
    protected static final String LIST_BUTTON = "List";
    protected static final String DEFAULT_STYLE = "default";

    protected static final String APPROVE_BUTTON = "Approve";
    protected static final String REJECT_BUTTON = "Reject ";


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

        final HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        getListRequestsButton( controlsContent );

        gridLayout.addComponent( controlsContent,0,0 );

    }


    private void getListRequestsButton( final HorizontalLayout controlsContent )
    {
        Button listRequests = new Button( LIST_BUTTON );
        listRequests.setId( "listRequests" );
        listRequests.addStyleName( DEFAULT_STYLE );
        listRequests.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                listRequests();
            }
        } );
        controlsContent.addComponent( listRequests );
    }


    private Table drawTable( String name, Set<ContainerInfo> containers )
    {
        final Table table = new Table( name );
        table.setId( name );

        table.setStyleName( "Reindeer.TABLE_ASTRONG" );

        table.setColumnCollapsingAllowed( true );
        //container name
        table.addContainerProperty( TableAttributes.CONTAINER_NAME, String.class, null );
        //container ip address
        table.addContainerProperty( TableAttributes.INTERFACES, String.class, null );
        //container parent table
        table.addContainerProperty( TableAttributes.TEMPLATE, String.class, null );
        //collapse column
        table.setColumnCollapsed( TableAttributes.TEMPLATE, true );
        //container vlan
        table.addContainerProperty( TableAttributes.VLAN, String.class, null );
        //container gpg key
        table.addContainerProperty( TableAttributes.GPG_PK, String.class, null );
        //collaps columb
        table.setColumnCollapsed( TableAttributes.GPG_PK, true );
        //available operations per container
        //TODO Implement operations per container
        //table.addContainerProperty( TableAttributes.AVAILABLE_OPERATIONS, String.class, null );

        if ( containers.size() == 0 )
        {
            return table;
        }
        HorizontalLayout availableOperations = new HorizontalLayout();

        for ( ContainerInfo containerInfo : containers )
        {
            table.addItem( new Object[] {
                    containerInfo.getHostname(), containerInfo.getInterfaces().toString(),
                    containerInfo.getTemplateName(), containerInfo.getVlan(), containerInfo.getPublicKey()
            }, null );
        }
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
        protected static final String INTERFACES = "INTERFACES";
        protected static final String TEMPLATE = "Template";
    }
}
