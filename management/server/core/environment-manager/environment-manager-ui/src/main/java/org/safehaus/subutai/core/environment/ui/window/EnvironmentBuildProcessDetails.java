package org.safehaus.subutai.core.environment.ui.window;


import com.vaadin.ui.TextArea;


/**
 * Created by bahadyr on 9/14/14.
 */
public class EnvironmentBuildProcessDetails extends DetailsWindow
{

    public EnvironmentBuildProcessDetails( final String caption )
    {
        super( caption );
    }


    public void setContent( String content )
    {
        TextArea area = getTextArea();
        area.setValue( content );
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
