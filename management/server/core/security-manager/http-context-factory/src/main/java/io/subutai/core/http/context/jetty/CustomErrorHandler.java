package io.subutai.core.http.context.jetty;


import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.handler.ErrorHandler;

import org.apache.commons.io.IOUtils;


public class CustomErrorHandler extends ErrorHandler
{

    @Override
    protected void writeErrorPageBody( final HttpServletRequest request, final Writer writer, final int code,
                                       final String message, final boolean showStacks ) throws IOException
    {
        writer.write(
                "<div style='position: absolute; top: 50%; margin-left: -162px; left: 50%; margin-top: -206px; width:"
                        + " 325px;'>" );
        writer.write( IOUtils.toString( CustomErrorHandler.class.getClassLoader().getResourceAsStream( "logo.svg" ),
                StandardCharsets.UTF_8 ) );
        writer.write( "<br><br><br>" );
        writer.write( "<center>Subutai Console is loading, please wait...</center>" );
        writer.write( "</div>" );
    }
}
