package org.safehaus.subutai.core.environment.ui.window;


import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;


/**
 * Created by bahadyr on 6/26/14.
 */
public class BlueprintDetails extends Window
{

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public BlueprintDetails( String caption )
    {
        setCaption( caption );
        setModal( true );
        setClosable( true );
        setVisible( false );
        setWidth( 900, UNITS_PIXELS );
        setHeight( 500, UNITS_PIXELS );
    }


    public void setContent( EnvironmentBlueprint blueprint )
    {
        String value = GSON.toJson( blueprint );
        TextArea area = getTextArea();
        area.setValue( value );
        setContent( area );
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
