package io.subutai.webui;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NotFound extends HttpServlet
{
    private static final Logger logger = LoggerFactory.getLogger( Login.class );


    @Override
    protected void doGet( final HttpServletRequest req, final HttpServletResponse resp )
            throws ServletException, IOException
    {
        try
        {
            setResponse( resp, "Oops, you did it again", 404 );
        }
        catch ( Exception e )
        {
            logger.error( "Error in doGet: {}", e.getMessage() );
        }
    }


    private void setResponse( HttpServletResponse response, String status, int statusCode )
    {

        try
        {
            response.getWriter().write( status );
            response.setStatus( statusCode );
        }
        catch ( IOException e )
        {
            logger.error( "Could not send response: {}", e.getMessage() );
        }
    }
}
