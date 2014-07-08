package org.safehaus.subutai.dis.manager.window;


import org.safehaus.subutai.api.manager.helper.Blueprint;
import org.safehaus.subutai.api.manager.util.BlueprintParser;

import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * Created by bahadyr on 6/26/14.
 */
public class BlueprintDetails extends Window {

    private Blueprint blueprint;
    private TextArea blueprintTxtArea;

    VerticalLayout verticalLayout = getVLayout();


    public BlueprintDetails(Blueprint blueprint) {
        setCaption( "Blueprint details" );
        setModal( true );
        setClosable( true );
        setVisible( false );
        setWidth( 600, UNITS_PIXELS );
        setHeight( 400, UNITS_PIXELS );
        blueprintTxtArea = getTextArea();
        BlueprintParser blueprintParser = new BlueprintParser();
        String blueprintStr = blueprintParser.parseBlueprint( blueprint );
        blueprintTxtArea.setValue( blueprintStr );
        verticalLayout.addComponent( blueprintTxtArea );

        setContent( verticalLayout );
    }


    public void setBlueprint( final Blueprint blueprint ) {
        this.blueprint = blueprint;
    }


    private VerticalLayout getVLayout() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin( true );
        content.setSpacing( true );
        return content;
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
