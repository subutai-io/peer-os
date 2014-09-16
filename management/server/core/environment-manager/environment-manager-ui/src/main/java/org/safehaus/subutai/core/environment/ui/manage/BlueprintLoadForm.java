package org.safehaus.subutai.core.environment.ui.manage;


import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;

import com.google.gson.JsonSyntaxException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class BlueprintLoadForm {

    private final VerticalLayout contentRoot;
    private TextArea blueprintTxtArea;
    private EnvironmentManagerUI managerUI;


    public BlueprintLoadForm( EnvironmentManagerUI managerUI ) {
        this.managerUI = managerUI;

        contentRoot = new VerticalLayout();

        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        blueprintTxtArea = getTextArea();

        Button loadBlueprintButton = new Button( "Save blueprint" );

        loadBlueprintButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                uploadBlueprint();
            }
        } );

        contentRoot.addComponent( blueprintTxtArea );
        contentRoot.addComponent( loadBlueprintButton );
    }


    private TextArea getTextArea() {
        blueprintTxtArea = new TextArea( "Blueprint" );
        blueprintTxtArea.setSizeFull();
        blueprintTxtArea.setRows( 20 );
        blueprintTxtArea.setImmediate( true );
        blueprintTxtArea.setWordwrap( false );
        return blueprintTxtArea;
    }


    private void uploadBlueprint() {

        try
        {
            boolean result =
                    managerUI.getEnvironmentManager().saveBlueprint( blueprintTxtArea.getValue().toString().trim() );
            if ( !result )
            {
                Notification
                        .show( "Error saving blueprint", "Check blueprint format", Notification.Type.WARNING_MESSAGE );
            }
            else
            {
                Notification.show( "Blueprint saved", "Blueprint saved", Notification.Type.HUMANIZED_MESSAGE );
            }
        }
        catch ( JsonSyntaxException e )
        {
            Notification.show( "Error saving blueprint", "Check blueprint format", Notification.Type.WARNING_MESSAGE );
        }
    }


    public VerticalLayout getContentRoot() {
        return this.contentRoot;
    }
}
