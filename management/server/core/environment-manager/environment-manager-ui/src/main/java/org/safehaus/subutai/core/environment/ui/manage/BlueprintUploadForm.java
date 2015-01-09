package org.safehaus.subutai.core.environment.ui.manage;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings( "serial" )
public class BlueprintUploadForm
{

    private static final String PLEASE_PROVIDE_A_BLUEPRINT = "Please provide a blueprint";
    private static final String ERROR_SAVING_BLUEPRINT = "Error saving blueprint. Please check format.";
    private static final String BLUEPRINT_SAVED = "Blueprint saved";
    private static final String SAVE = "Save";
    private static final String BLUEPRINT = "Blueprint";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final VerticalLayout contentRoot;
    private TextArea textArea;
    private EnvironmentManagerPortalModule managerUI;


    public BlueprintUploadForm( EnvironmentManagerPortalModule managerUI )
    {
        this.managerUI = managerUI;

        contentRoot = new VerticalLayout();

        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        textArea = getTextArea();

        Button loadBlueprintButton = new Button( SAVE );
        loadBlueprintButton.setId( "loadBlueprintButton" );

        loadBlueprintButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                uploadAndSaveBlueprint();
            }
        } );

        Button putSampleBlueprint = new Button( "Get sample" );
        putSampleBlueprint.setId( "putSampleBlueprint" );
        putSampleBlueprint.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {


                String blueprintStr = GSON.toJson( getSampleBlueprint() );
                textArea.setValue( blueprintStr );
            }
        } );

        contentRoot.addComponent( textArea );
        contentRoot.addComponent( loadBlueprintButton );
        contentRoot.addComponent( putSampleBlueprint );
    }


    private EnvironmentBlueprint getSampleBlueprint()
    {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setDomainName( "intra.lan" );
        environmentBlueprint.setExchangeSshKeys( true );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setName( "My cassandra + master environment" );

        Set<NodeGroup> groups = new HashSet<>();

        NodeGroup nodeGroup1 = new NodeGroup();
        nodeGroup1.setDomainName( "intra.lan" );
        nodeGroup1.setName( "Cassandra node group" );
        nodeGroup1.setLinkHosts( true );
        nodeGroup1.setExchangeSshKeys( true );
        nodeGroup1.setNumberOfNodes( 2 );
        nodeGroup1.setPlacementStrategy( new PlacementStrategy( "ROUND_ROBIN" ) );
        nodeGroup1.setTemplateName( "cassandra" );

        NodeGroup nodeGroup2 = new NodeGroup();
        nodeGroup2.setDomainName( "intra.lan" );
        nodeGroup2.setName( "Master node group" );
        nodeGroup2.setLinkHosts( true );
        nodeGroup2.setExchangeSshKeys( true );
        nodeGroup2.setNumberOfNodes( 2 );
        nodeGroup2.setPlacementStrategy( new PlacementStrategy( "ROUND_ROBIN" ) );
        nodeGroup2.setTemplateName( "master" );

        groups.add( nodeGroup1 );
        groups.add( nodeGroup2 );

        environmentBlueprint.setNodeGroups( groups );
        return environmentBlueprint;
    }


    private TextArea getTextArea()
    {
        textArea = new TextArea( BLUEPRINT );
        textArea.setId( "textArea" );
        textArea.setSizeFull();
        textArea.setRows( 20 );
        textArea.setImmediate( true );
        textArea.setWordwrap( false );
        return textArea;
    }


    private void uploadAndSaveBlueprint()
    {
        String content = textArea.getValue().trim();
        if ( content.length() > 0 )
        {
            try
            {
                managerUI.getEnvironmentManager().saveBlueprint( content );
                Notification.show( BLUEPRINT_SAVED );
            }
            catch ( EnvironmentManagerException e )
            {
                Notification.show( ERROR_SAVING_BLUEPRINT );
            }
        }
        else
        {
            Notification.show( PLEASE_PROVIDE_A_BLUEPRINT );
        }
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
