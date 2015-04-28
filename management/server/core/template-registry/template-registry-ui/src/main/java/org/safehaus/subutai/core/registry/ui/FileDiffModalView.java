package org.safehaus.subutai.core.registry.ui;


import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;


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
        StringBuilder result = new StringBuilder( "" );
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
            if ( str.startsWith( "diff --git" ) )
            {
                append = false;
                continue;
            }
            if ( append )
            {
                if ( str.startsWith( "-" ) )
                {
                    boolean aDeleted = true;
                    int j = i;
                    while ( j < parsedString.length && parsedString[j].startsWith( "-" ) )
                    {
                        j++;
                    }
                    if ( j < parsedString.length && parsedString[j].startsWith( "+" ) )
                    {
                        aDeleted = false;
                    }
                    if ( aDeleted )
                    {
                        for (; i < parsedString.length && parsedString[i].startsWith( "-" ); i++ )
                        {
                            String lineFormat = "<div style=\"background-color: #ffb6ba;\">";
                            str = parsedString[i];
                            str = str.length() > 0 ? " " + str.substring( 1 ) : str;
                            lineFormat += "<pre>" + str + "</pre>";
                            lineFormat += "</div>";
                            result.append( lineFormat );
                        }
                        if ( !( i == parsedString.length - 1 ) )
                        {
                            i--;
                        }
                    }
                    else
                    {
                        for ( i = j; i < parsedString.length && parsedString[i].startsWith( "+" ); i++ )
                        {
                            String lineFormat = "<div style=\"background-color: #dfd;\">";
                            str = parsedString[i];
                            str = str.length() > 0 ? " " + str.substring( 1 ) : str;
                            lineFormat += "<pre>" + str + "</pre>";
                            lineFormat += "</div>";
                            result.append( lineFormat );
                        }
                        if ( !( i == parsedString.length - 1 ) )
                        {
                            i--;
                        }
                    }
                }
                else if ( str.startsWith( "+" ) )
                {
                    String lineFormat = "<div style=\"background-color: ";
                    //New line added representation
                    lineFormat += "#97f295;";
                    str = str.length() > 0 ? " " + str.substring( 1 ) : str;

                    lineFormat += "\">";
                    lineFormat += "<pre>" + str + "</pre>";
                    lineFormat += "</div>";
                    result.append( lineFormat );
                }
                else if ( !"\\ No newline at end of file".equals( str ) )
                {
                    String lineFormat = "<div style=\"background-color: ";
                    lineFormat += "\">";
                    lineFormat += "<pre>" + str + "</pre>";
                    lineFormat += "</div>";
                    result.append( lineFormat );
                }
            }
        }
        if ( !append )
        {
            int inx = input.indexOf( "Binary" );
            result.append( "<pre>" ).append( input.substring( inx > 0 ? inx : 0 ) ).append( "</pre>" );
        }
        return result.toString();
    }
}
