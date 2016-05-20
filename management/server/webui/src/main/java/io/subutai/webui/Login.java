package io.subutai.webui;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;


public class Login extends HttpServlet
{
    private static final Logger logger = LoggerFactory.getLogger( Login.class );


    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException
    {

        String username = request.getParameter( "username" );
        String password = request.getParameter( "password" );
        String sptoken = request.getParameter( "sptoken" );
        User user = null;

        try
        {
            if ( !Strings.isNullOrEmpty( username ) )
            {
                IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );

                if ( identityManager != null )
                {
                    sptoken = identityManager.getUserToken( username, password );
                    user = identityManager.authenticateByToken( sptoken );
                }
            }
            else if ( !Strings.isNullOrEmpty( sptoken ) )
            {
                IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );

                if ( identityManager != null )
                {
                    user = identityManager.authenticateByToken( sptoken );
                }
            }
            else
            {
                request.setAttribute( "error", "Please enter username or password" );
                response.getWriter().write( "Error, Please enter username or password" );
                response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
            }


            if ( user != null )
            {
                request.getSession().setAttribute( "userSessionData", sptoken );
                Cookie ctoken = new Cookie( "sptoken", sptoken );
                //                    sptoken.setMaxAge( 3600 * 24 * 7 * 365 * 10 );

                logger.info( user.getFingerprint() );
                logger.info( user.getEmail() );
                logger.info( user.getFullName() );
                logger.info( user.getSecurityKeyId() );
                logger.info( user.getUserName() );
                Cookie fingerprint = new Cookie( "su_fingerprint", user.getFingerprint() );
                //                    fingerprint.setMaxAge( 3600 * 24 * 7 * 365 * 10 );

                response.addCookie( ctoken );
                response.addCookie( fingerprint );
            }
            else
            {
                request.setAttribute( "error", "Wrong Auth Credentials !!!" );
                response.getWriter().write( "Wrong Auth Credentials" );
                response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
            }
        }
        catch ( Exception e )
        {
            request.setAttribute( "error", "karaf exceptions !!!" );
            response.getWriter().write( "Error: " + e.getMessage() );
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
    }


    @Override
    protected void doGet( final HttpServletRequest req, final HttpServletResponse resp )
            throws ServletException, IOException
    {
        doPost( req , resp );
        resp.sendRedirect( "/" );
    }
}