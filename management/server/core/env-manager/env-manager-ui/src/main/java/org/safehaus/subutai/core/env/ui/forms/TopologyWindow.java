package org.safehaus.subutai.core.env.ui.forms;


import org.safehaus.subutai.core.env.api.build.Blueprint;

import com.vaadin.ui.Window;


public class TopologyWindow
{
    private final Window window;
    private final Blueprint blueprint;


    public TopologyWindow( Blueprint blueprint )
    {
        this.blueprint = blueprint;
        window = new Window();
        window.setCaption( "Topology" );
        window.setWidth( "800px" );
        window.setHeight( "600px" );
        window.setModal( true );
        window.setClosable( true );
    }


    public Window getWindow()
    {
        return window;
    }
}
