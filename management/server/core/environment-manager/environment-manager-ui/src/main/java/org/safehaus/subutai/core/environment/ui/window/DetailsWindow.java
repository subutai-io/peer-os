package org.safehaus.subutai.core.environment.ui.window;


import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * Created by bahadyr on 7/4/14.
 */
public abstract class DetailsWindow extends Window
{

    //    public TextArea blueprintTxtArea;

    public VerticalLayout verticalLayout = getVLayout();


    public DetailsWindow( String caption )
    {
        setCaption( caption );
        setModal( true );
        setClosable( true );
        setVisible( false );
        setWidth( 800, Unit.PIXELS );
        setHeight( 600, Unit.PIXELS );
        //        blueprintTxtArea = getTextArea();
        //        verticalLayout.addComponent( blueprintTxtArea );
        setContent( verticalLayout );
    }


    private VerticalLayout getVLayout()
    {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin( true );
        content.setSpacing( true );
        return content;
    }


    //    public abstract void setContent(EnvironmentBlueprint blueprint);
}
