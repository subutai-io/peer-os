package org.safehaus.subutai.core.environment.ui.window;


import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;

import com.vaadin.ui.TextArea;


/**
 * Created by bahadyr on 6/26/14.
 */
public class BlueprintDetails extends DetailsWindow
{

    private EnvironmentManager environmentManager;


    public BlueprintDetails( String caption, EnvironmentManager environmentManager )
    {
        super( caption );
        this.environmentManager = environmentManager;
    }


    public void setContent( EnvironmentBlueprint blueprint )
    {
        String value = environmentManager.parseBlueprint( blueprint );
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
