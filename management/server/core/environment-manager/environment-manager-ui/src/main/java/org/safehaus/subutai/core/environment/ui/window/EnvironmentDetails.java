package org.safehaus.subutai.core.environment.ui.window;


import com.vaadin.ui.Window;


/**
 * Created by bahadyr on 7/4/14.
 */
public class EnvironmentDetails extends Window
{

    public EnvironmentDetails( final String caption )
    {
        setCaption( caption );
        setModal( true );
        setClosable( true );
        setVisible( false );
        setWidth( 900, UNITS_PIXELS );
//        setHeight( 500, UNITS_PIXELS );
    }
}
