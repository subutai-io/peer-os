package org.safehaus.subutai.dis.manager.manage;


import org.safehaus.subutai.api.manager.EnvironmentManager;

import com.vaadin.ui.Button;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings( "serial" )
public class BlueprintLoadForm {

    private final VerticalLayout contentRoot;
    private TextArea blueprintTxtArea;
    private EnvironmentManager environmentManager;


    public BlueprintLoadForm(EnvironmentManager environmentManager) {
        this.environmentManager = environmentManager;

        contentRoot = new VerticalLayout();

        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        blueprintTxtArea = getTextArea();

        Button loadBlueprintButton = new Button("Load blueprint");

        loadBlueprintButton.addListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                  uploadBlueprint();
            }
        } );

        contentRoot.addComponent( blueprintTxtArea );
        contentRoot.addComponent( loadBlueprintButton );
    }


    public VerticalLayout getContentRoot() {
        return this.contentRoot;
    }

    private void uploadBlueprint() {
          environmentManager.saveBlueprint( blueprintTxtArea.getValue().toString().trim() );
    }

    private TextArea getTextArea() {
        blueprintTxtArea = new TextArea( "Blueprint" );
        blueprintTxtArea.setSizeFull();
        blueprintTxtArea.setRows( 20 );
        blueprintTxtArea.setImmediate( true );
        blueprintTxtArea.setWordwrap( false );
        return blueprintTxtArea;
    }
}
