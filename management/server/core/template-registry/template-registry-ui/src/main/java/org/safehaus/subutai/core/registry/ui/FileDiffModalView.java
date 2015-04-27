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
                //TODO correctly format output git diff string for showing precise changes
                //do not delete these comments
                //                List<String> aChanges = new ArrayList<>();
                //                List<String> aChangesDuplicate = new ArrayList<>();
                //                for (; i < parsedString.length && parsedString[i].startsWith( "-" ); i++ )
                //                {
                //                    str = parsedString[i];
                //                    String temp = str.replace( ".", "\\." );
                //                    temp = temp.replace( "?", "\\?" );
                //                    temp = temp.replace( "+", "\\+" );
                //                    temp = temp.replace( "*", "\\*" );
                //                    temp = temp.replace( "^", "\\^" );
                //                    temp = temp.replace( "$", "\\$" );
                //                    temp = temp.replace( "\\", "\\\\" );
                //                    temp = temp.replace( "(", "\\(" );
                //                    temp = temp.replace( ")", "\\)" );
                //                    temp = temp.replace( "[", "\\[" );
                //                    temp = temp.replace( "]", "\\]" );
                //                    temp = temp.replace( "{", "\\{" );
                //                    temp = temp.replace( "}", "\\}" );
                //                    temp = temp.replace( "|", "\\|" );
                //
                //                    String tokens [] = temp.split( "[(\\s|\t)]" );
                //                    temp = "(";
                //                    for ( int j = 0; j < tokens.length; j++ )
                //                    {
                //                        String token = tokens[j];
                //                        if ( j == tokens.length - 1 )
                //                        {
                //                            temp += token;
                //                        }
                //                        else
                //                        {
                //                            temp += token + " | ";
                //                        }
                //                    }
                //                    temp += ")";
                //
                //                    aChanges.add( temp.length() > 0 ? " " + temp.substring( 1 ) : temp );
                //                    aChangesDuplicate.add( str.length() > 0 ? " " + str.substring( 1 ) : str );
                //                }
                //                List<String> bChanges = new ArrayList<>();
                //                for (; i < parsedString.length && parsedString[i].startsWith( "+" ); i++ )
                //                {
                //                    str = parsedString[i];
                //                    //                    String temp = str.replace( ".", "\\." );
                //                    //                    temp = temp.replace( "?", "\\?" );
                //                    //                    temp = temp.replace( "+", "\\+" );
                //                    //                    temp = temp.replace( "*", "\\*" );
                //                    //                    temp = temp.replace( "^", "\\^" );
                //                    //                    temp = temp.replace( "$", "\\$" );
                //                    //                    temp = temp.replace( "\\", "\\\\" );
                //                    //                    temp = temp.replace( "(", "\\(" );
                //                    //                    temp = temp.replace( ")", "\\)" );
                //                    //                    temp = temp.replace( "[", "\\[" );
                //                    //                    temp = temp.replace( "]", "\\]" );
                //                    //                    temp = temp.replace( "{", "\\{" );
                //                    //                    temp = temp.replace( "}", "\\}" );
                //                    //                    temp = temp.replace( "|", "\\|" );
                //                    bChanges.add( str.length() > 0 ? " " + str.substring( 1 ) : str );
                //                }
                //
                //                for ( int j = 0; j < aChanges.size(); j++ )
                //                {
                //                    String aChange = aChanges.get( j );
                //                    Pattern p = Pattern.compile( "[" + aChange + "]" );
                //                    for ( final String bChange : bChanges )
                //                    {
                //                        String lineFormat = "<div style=\"background-color: ";
                //                        Matcher matcher = p.matcher( bChange );
                //                        if ( matcher.find() )
                //                        {
                //                            lineFormat += "#dfd;";
                //                        }
                //                    }
                //                }


                if ( str.startsWith( "-" ) )
                {

                    boolean aDeleted = true;
                    int j = i;
                    for (; j < parsedString.length && parsedString[j].startsWith( "-" ); j++ )
                    {
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
            result.append( "<pre>" + input.substring( inx > 0 ? inx : 0 ) + "</pre>" );
        }
        return result.toString();
    }
}
