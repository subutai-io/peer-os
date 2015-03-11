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
    public FileDiffModalView( final String caption, final HorizontalLayout content, final String aVersion )
    {

        super( caption, content );

        content.setSpacing( true );
        content.setSizeFull();

        final Label aText = new Label( formattedStringWithStyles( aVersion ) );
        aText.setImmediate( true );
        aText.setContentMode( ContentMode.HTML );

        content.addComponent( aText );

        content.setExpandRatio( aText, 0.5f );

        setImmediate( true );
        setModal( true );
        setResizable( true );
        center();
        setWidth( 65, Unit.PERCENTAGE );
        setHeight( "40%" );
    }


    private String formattedStringWithStyles( String input )
    {
        String result = "";
        String parsedString[] = input.split( "\n" );
        boolean append = false;
        for ( int i = 0; i < parsedString.length; i++ )
        {
            String str = parsedString[i];
            if ( str.startsWith( "@@" ) )
            {
                append = true;
                continue;
            }
            if ( append )
            {
                String temp = "<div style=\"background-color: ";
                if ( str.startsWith( "-" ) )
                {
                    if ( i + 1 < parsedString.length )
                    {
                        String nextLine = parsedString[i + 1];
                        if ( nextLine.startsWith( "+" ) )
                        {
                            temp += "rgba(73, 60, 186, 0.53);";
                        }
                        else
                        {
                            temp += "rgba(189, 67, 51, 0.62);";
                        }
                    }
                }
                else if ( str.startsWith( "+" ) )
                {
                    temp += "rgba(34, 134, 58, 0.54);";
                }
                temp += "\">";
                temp += str;
                temp += "</div>";
                result += temp;
            }
        }
        return result;
    }
}
