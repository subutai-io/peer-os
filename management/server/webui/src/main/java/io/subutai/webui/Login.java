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

import io.subutai.common.security.exception.IdentityExpiredException;
import io.subutai.common.security.exception.InvalidLoginException;
import io.subutai.common.security.exception.SessionBlockedException;
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
        String newPassword = request.getParameter( "newpassword" );
        User user = null;


        try
        {
            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );


            if( !Strings.isNullOrEmpty( newPassword ))
            {
                identityManager.changeUserPassword( username, password, newPassword );
                password = newPassword;
            }

            try
            {
                if ( !Strings.isNullOrEmpty( username ) )
                {
                    if ( identityManager != null )
                    {
                        sptoken = identityManager.getNewUserToken( username, password );
                        user = identityManager.authenticateByToken( sptoken );
                    }
                    else
                    {
                        throw new Exception( "Karaf Auth Module is loading, please try again later" );
                    }
                }
                else if ( !Strings.isNullOrEmpty( sptoken ) )
                {
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

                authenticateUser( request, response, user, sptoken );
            }
            catch ( IdentityExpiredException e )
            {
                request.setAttribute( "error", "Your credentials are expired  !!!" );
                response.getWriter().write( "Auth Credentials are expired" );
                response.setStatus( HttpServletResponse.SC_PRECONDITION_FAILED );
            }
            catch ( SessionBlockedException e )
            {
                request.setAttribute( "error", "Account is blocked !!!" );
                response.getWriter().write( "Account is blocked" );
                response.setStatus( HttpServletResponse.SC_FORBIDDEN );
            }
            catch ( InvalidLoginException e )
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

    private void authenticateUser( HttpServletRequest request, HttpServletResponse response, User user, String sptoken )
            throws InvalidLoginException
    {
        if ( user == null )
        {
            throw new InvalidLoginException();
        }
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

    @Override
    protected void doGet( final HttpServletRequest req, final HttpServletResponse resp )
            throws ServletException, IOException
    {
        doPost( req , resp );
        resp.sendRedirect( "/" );
    }
}