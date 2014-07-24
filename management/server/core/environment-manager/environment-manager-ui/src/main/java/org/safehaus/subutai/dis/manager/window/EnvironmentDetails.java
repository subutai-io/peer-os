package org.safehaus.subutai.dis.manager.window;


import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;

import com.vaadin.ui.TextArea;


/**
 * Created by bahadyr on 7/4/14.
 */
public class EnvironmentDetails extends DetailsWindow {


    public EnvironmentDetails( final String caption ) {
        super( caption );
    }


    public void setContent( final Environment blueprint ) {
        StringBuilder sb = new StringBuilder();
        sb.append( blueprint.getName() ).append( "\n" );
        if ( blueprint.getNodes() != null ) {
                for ( Node node : blueprint.getNodes() ) {
                    sb.append( node.getAgent().getHostname() ).append( "\n" );
            }
        }

        TextArea area = getTextArea();
        area.setValue( sb.toString() );
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
