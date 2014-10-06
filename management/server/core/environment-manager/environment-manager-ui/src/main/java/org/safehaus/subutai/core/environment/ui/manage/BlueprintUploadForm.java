package org.safehaus.subutai.core.environment.ui.manage;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class BlueprintUploadForm
{

    private static final String PLEASE_PROVIDE_A_BLUEPRINT = "Please provide a blueprint";
    private static final String ERROR_SAVING_BLUEPRINT = "Error saving blueprint. Please check format.";
    private static final String BLUEPRINT_SAVED = "Blueprint saved";
    private final VerticalLayout contentRoot;
    private TextArea blueprintTxtArea;
    private EnvironmentManagerPortalModule managerUI;


    public BlueprintUploadForm( EnvironmentManagerPortalModule managerUI )
    {
        this.managerUI = managerUI;

        contentRoot = new VerticalLayout();

        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        blueprintTxtArea = getTextArea();

        Button loadBlueprintButton = new Button( "Save" );

        loadBlueprintButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                uploadAndSaveBlueprint();
            }
        } );

        Button putSampleBlueprint = new Button( "Get sample" );
        putSampleBlueprint.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {


                String blueprintStr = GSON.toJson( getSampleBlueprint() );
                blueprintTxtArea.setValue( blueprintStr );
            }
        } );

        contentRoot.addComponent( blueprintTxtArea );
        contentRoot.addComponent( loadBlueprintButton );
        contentRoot.addComponent( putSampleBlueprint );
    }


    private EnvironmentBlueprint getSampleBlueprint()
    {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setDomainName( "intra.lan" );
        environmentBlueprint.setExchangeSshKeys( true );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setName( "My environment" );

        Set<NodeGroup> groups = new HashSet<>();

        NodeGroup nodeGroup1 = new NodeGroup();
        nodeGroup1.setDomainName( "intra.lan" );
        nodeGroup1.setName( "Some name" );
        nodeGroup1.setLinkHosts( true );
        nodeGroup1.setExchangeSshKeys( true );
        nodeGroup1.setNumberOfNodes( 2 );
        nodeGroup1.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        nodeGroup1.setTemplateName( "master" );

        NodeGroup nodeGroup2 = new NodeGroup();
        nodeGroup2.setDomainName( "intra.lan" );
        nodeGroup2.setName( "Some name" );
        nodeGroup2.setLinkHosts( true );
        nodeGroup2.setExchangeSshKeys( true );
        nodeGroup2.setNumberOfNodes( 2 );
        nodeGroup2.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        nodeGroup2.setTemplateName( "master" );

        groups.add( nodeGroup1 );
        groups.add( nodeGroup2 );

        environmentBlueprint.setNodeGroups( groups );
        return environmentBlueprint;
    }


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    private TextArea getTextArea()
    {
        blueprintTxtArea = new TextArea( "Blueprint" );
        blueprintTxtArea.setSizeFull();
        blueprintTxtArea.setRows( 20 );
        blueprintTxtArea.setImmediate( true );
        blueprintTxtArea.setWordwrap( false );
        return blueprintTxtArea;
    }


    private void uploadAndSaveBlueprint()
    {
        String content = blueprintTxtArea.getValue().trim();
        if ( content.length() > 0 )
        {
            boolean result = managerUI.getEnvironmentManager().saveBlueprint( content );
            if ( !result )
            {
                Notification.show( ERROR_SAVING_BLUEPRINT );
            }
            else
            {
                Notification.show( BLUEPRINT_SAVED );
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
