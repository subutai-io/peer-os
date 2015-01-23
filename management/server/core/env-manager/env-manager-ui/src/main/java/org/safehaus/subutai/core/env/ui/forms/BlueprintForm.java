package org.safehaus.subutai.core.env.ui.forms;


import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.api.build.Blueprint;
import org.safehaus.subutai.core.env.api.build.NodeGroup;

import com.google.common.collect.Sets;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;


public class BlueprintForm
{
    private static final String BLUEPRINT = "Blueprint";
    private static final String SAVE = "Save";
    private static final String PLEASE_PROVIDE_A_BLUEPRINT = "Please provide a blueprint";
    private static final String ERROR_SAVING_BLUEPRINT = "Error saving blueprint";
    private static final String BLUEPRINT_SAVED = "Blueprint saved";


    private final VerticalLayout contentRoot;
    private final EnvironmentManager environmentManager;
    private TextArea textArea;


    public BlueprintForm( EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
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


                String blueprintStr = JsonUtil.toJson( getSampleBlueprint() );
                textArea.setValue( blueprintStr );
            }
        } );

        contentRoot.addComponent( textArea );
        contentRoot.addComponent( loadBlueprintButton );
        contentRoot.addComponent( putSampleBlueprint );
    }


    private Blueprint getSampleBlueprint()
    {
        NodeGroup nodeGroup = environmentManager
                .newNodeGroup( "Sample node group", "master", Common.DEFAULT_DOMAIN_NAME, 2, 0, 0,
                        new PlacementStrategy( "ROUND_ROBIN" ) );
        return new Blueprint( "Sample blueprint", Sets.newHashSet( nodeGroup ) );
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
                Blueprint blueprint = JsonUtil.fromJson( content, Blueprint.class );
                environmentManager.saveBlueprint( blueprint );
                Notification.show( BLUEPRINT_SAVED );
            }
            catch ( Exception e )
            {
                Notification.show( ERROR_SAVING_BLUEPRINT, e.getMessage(), Notification.Type.ERROR_MESSAGE );
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
