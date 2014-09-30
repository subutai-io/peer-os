package org.safehaus.subutai.core.environment.ui.window;


import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * Created by bahadyr on 7/4/14.
 */
public abstract class DetailsWindow extends Window
{

    public VerticalLayout verticalLayout = getVLayout();


    public DetailsWindow( String caption )
    {

    }


    private VerticalLayout getVLayout()
    {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin( true );
        content.setSpacing( true );
        return content;
    }
}
