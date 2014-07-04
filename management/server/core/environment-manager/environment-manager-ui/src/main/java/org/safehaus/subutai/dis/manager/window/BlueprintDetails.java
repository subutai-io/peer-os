package org.safehaus.subutai.dis.manager.window;


import org.safehaus.subutai.api.manager.helper.Blueprint;
import org.safehaus.subutai.api.manager.helper.EnvironmentBlueprint;
import org.safehaus.subutai.api.manager.util.BlueprintParser;

import com.vaadin.ui.TextArea;


/**
 * Created by bahadyr on 6/26/14.
 */
public class BlueprintDetails extends DetailsWindow {

    private BlueprintParser blueprintParser;


    public BlueprintDetails( String caption ) {
        super( caption );
        this.blueprintParser = new BlueprintParser();
    }


    @Override
    public void setContent( Blueprint blueprint ) {
        String value = blueprintParser.parseEnvironmentBlueprint( ( EnvironmentBlueprint ) blueprint );
        TextArea area = getTextArea();
        area.setValue( value );
        verticalLayout.addComponent( area );
    }


    private TextArea getTextArea() {
        TextArea textArea = new TextArea( "Blueprint" );
        textArea.setSizeFull();
        textArea.setRows( 20 );
        textArea.setImmediate( true );
        textArea.setWordwrap( false );
        return textArea;
    }
}
