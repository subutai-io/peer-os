package org.safehaus.subutai.core.environment.ui.window;


import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.ui.TextArea;


/**
 * Created by bahadyr on 6/26/14.
 */
public class BlueprintDetails extends DetailsWindow
{

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public BlueprintDetails( String caption )
    {
        super( caption );
    }


    public void setContent( EnvironmentBlueprint blueprint )
    {
        String value = GSON.toJson( blueprint );
        TextArea area = getTextArea();
        area.setValue( value );
        verticalLayout.addComponent( area );
    }


    private TextArea getTextArea()
    {
        TextArea textArea = new TextArea( "Blueprint" );
        textArea.setSizeFull();
        textArea.setRows( 20 );
        textArea.setImmediate( true );
        textArea.setWordwrap( false );
        return textArea;
    }
}
