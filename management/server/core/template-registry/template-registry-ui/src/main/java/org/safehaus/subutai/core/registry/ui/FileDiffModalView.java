package org.safehaus.subutai.core.registry.ui;


import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;


/**
 * Created by talas on 1/6/15.
 */
public class FileDiffModalView extends Window
{


    /**
     * Creates a new, empty window with the given content and title.
     *
     * @param caption the title of the window.
     */
    public FileDiffModalView( final String caption, final HorizontalLayout content, final String aVersion,
                              final String bVersion )
    {

        super( caption, content );

        content.setSpacing( true );
        content.setSizeFull();


        String customStyle =
                "<div style=\"text-align:center;color:blue;font-weight:bold; word-wrap: break-word; /* " + "IE  */\n"
                        + "   white-space: -moz-pre-wrap;/* Firefox */\">";

        final Label aText = new Label( customStyle + aVersion + "</div>" );
        aText.setImmediate( true );
        aText.setContentMode( ContentMode.HTML );

        final Label bText = new Label( customStyle + bVersion + "</div>" );
        bText.setImmediate( true );
        bText.setContentMode( ContentMode.HTML );

        content.addComponent( aText );
        content.addComponent( bText );

        content.setExpandRatio( aText, 0.5f );
        content.setExpandRatio( bText, 0.5f );

        setImmediate( true );
        setModal( true );
        setResizable( true );
        center();
        setWidth( 65, Unit.PERCENTAGE );
        setHeight( "40%" );
    }
}
