package org.safehaus.subutai.core.environment.ui.window;


import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;


/**
 * Created by bahadyr on 9/14/14.
 */
public class EnvironmentBuildProcessDetails extends Window
{

    public EnvironmentBuildProcessDetails( final String caption )
    {
        setCaption( caption );
        setModal( true );
        setClosable( true );
        setVisible( false );
        setWidth( 900, UNITS_PIXELS );
        setHeight( 500, UNITS_PIXELS );
    }


    public void setContent( String content )
    {
        TextArea area = getTextArea();
        area.setValue( content );
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
