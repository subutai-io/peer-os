package org.safehaus.subutai.dis.manager.window;


import org.safehaus.subutai.api.manager.helper.Blueprint;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.EnvironmentNodeGroup;
import org.safehaus.subutai.shared.protocol.Agent;

import com.vaadin.ui.TextArea;


/**
 * Created by bahadyr on 7/4/14.
 */
public class EnvironmentDetails extends DetailsWindow {


    public EnvironmentDetails( final String caption ) {
        super( caption );
    }


    @Override
    public void setContent( final Blueprint blueprint ) {
        Environment environment = ( Environment ) blueprint;
        StringBuilder sb = new StringBuilder();
        sb.append( environment.getName() ).append( "\n" );
        if ( environment.getEnvironmentNodeGroups() != null ) {
            for ( EnvironmentNodeGroup group : environment.getEnvironmentNodeGroups() ) {
                for ( Agent agent : group.getInstances() ) {
                    sb.append( agent.getHostname() ).append( "\n" );
                }
            }
        }

        TextArea area = getTextArea();
        area.setValue( sb.toString() );

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
