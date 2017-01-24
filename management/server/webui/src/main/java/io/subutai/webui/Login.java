package io.subutai.webui;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.security.exception.IdentityExpiredException;
import io.subutai.common.security.exception.InvalidLoginException;
import io.subutai.common.security.exception.SessionBlockedException;
import io.subutai.common.settings.Common;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;


public class Login extends HttpServlet
{
    private static final Logger logger = LoggerFactory.getLogger( Login.class );


    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException
    {
        try
        {
            String username = request.getParameter( "username" );
            String password = request.getParameter( "password" );
            String sptoken = request.getParameter( "sptoken" );
            String newPassword = request.getParameter( "newpassword" );
            User user;

            IdentityManager identityManager = ServiceLocator.getServiceOrNull( IdentityManager.class );

            Preconditions.checkNotNull( identityManager, "Karaf Auth Module is loading, please try again later" );

            if ( !Strings.isNullOrEmpty( newPassword ) )
            {
                identityManager.changeUserPassword( username, password, newPassword );
                password = newPassword;
            }

            if ( !Strings.isNullOrEmpty( username ) )
            {
                sptoken = identityManager.getUserToken( username, password );
                user = identityManager.authenticateByToken( sptoken );
            }
            else if ( !Strings.isNullOrEmpty( sptoken ) )
            {

                user = identityManager.authenticateByToken( sptoken );
            }
            else
            {
                request.setAttribute( "error", "Please enter username or password" );
                setResponse( response, "Error, Please enter username or password",
                        HttpServletResponse.SC_UNAUTHORIZED );

                return;
            }

            authenticateUser( request, response, user, sptoken );
        }
        catch ( IdentityExpiredException e )
        {
            request.setAttribute( "error", "Your credentials are expired  !!!" );
            setResponse( response, "Please create a new password. The old one is expired",
                    HttpServletResponse.SC_PRECONDITION_FAILED );
        }
        catch ( SessionBlockedException e )
        {
            request.setAttribute( "error", "Account is blocked !!!" );
            setResponse( response, "Account is blocked", HttpServletResponse.SC_FORBIDDEN );
        }
        catch ( InvalidLoginException e )
        {
            request.setAttribute( "error", "Wrong Auth Credentials !!!" );
            setResponse( response, "Wrong Auth Credentials", HttpServletResponse.SC_UNAUTHORIZED );
        }

        catch ( Exception e )
        {
            request.setAttribute( "error", "karaf exceptions !!!" );
            setResponse( response, "Error: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
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


    private void authenticateUser( HttpServletRequest request, HttpServletResponse response, User user, String sptoken )
            throws InvalidLoginException
    {
        if ( user == null )
        {
            throw new InvalidLoginException();
        }
        request.getSession().setAttribute( "userSessionData", sptoken );
        Cookie ctoken = new Cookie( "sptoken", sptoken );
        ctoken.setSecure( true );

        logger.info( user.getFingerprint() );
        logger.info( user.getEmail() );
        logger.info( user.getFullName() );
        logger.info( user.getSecurityKeyId() );
        logger.info( user.getUserName() );
        Cookie fingerprint = new Cookie( Common.E2E_PLUGIN_USER_KEY_FINGERPRINT_NAME, user.getFingerprint() );
        fingerprint.setSecure( true );


        if ( Strings.isNullOrEmpty( sptoken ) )
        {
            Cookie[] cookies = request.getCookies();
            for ( final Cookie cookie : cookies )
            {
                if ( "sptoken".equals( cookie.getName() ) )
                {
                    cookie.setValue( "" );
                    cookie.setPath( "/" );
                }
            }
        }

        response.addCookie( ctoken );
        response.addCookie( fingerprint );
    }


    @Override
    protected void doGet( final HttpServletRequest req, final HttpServletResponse resp )
            throws ServletException, IOException
    {
        try
        {
            doPost( req, resp );
            resp.sendRedirect( "/" );
        }
        catch ( Exception e )
        {
            logger.error( "Error in doGet: {}", e.getMessage() );
        }
    }
}