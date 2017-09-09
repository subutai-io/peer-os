package io.subutai.core.http.context.jetty;


import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.handler.ErrorHandler;

import org.apache.commons.io.IOUtils;


public class CustomErrorHandler extends ErrorHandler
{

    @Override
    protected void writeErrorPageBody( final HttpServletRequest request, final Writer writer, final int code,
                                       final String message, final boolean showStacks ) throws IOException
    {
        super.writeErrorPageBody( request, writer, code, message, showStacks );

        writer.write( "Oops, you did it again" );
        writer.write( "<div>" );
        writer.write( IOUtils.toString( CustomErrorHandler.class.getClassLoader().getResourceAsStream( "logo.svg" ),
                "UTF-8" ) );
        writer.write( "</div>" );
    }
}
