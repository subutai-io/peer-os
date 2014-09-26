package org.safehaus.subutai.core.environment.ui.manage;


import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;

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

        contentRoot.addComponent( blueprintTxtArea );
        contentRoot.addComponent( loadBlueprintButton );
    }


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
