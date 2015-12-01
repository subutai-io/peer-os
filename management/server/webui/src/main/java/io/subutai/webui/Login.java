package io.subutai.webui;


import java.io.IOException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;


public class Login extends HttpServlet
{
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException
    {
        String username = request.getParameter( "username" );
        String password = request.getParameter( "password" );

        if ( !Strings.isNullOrEmpty( username ) )
        {
            try
            {
                IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );

                String token = identityManager.getUserToken( username, password );

                if ( !Strings.isNullOrEmpty( token ) )
                {
                    request.getSession().setAttribute( "userSessionData", token );
                    Cookie cookie = new Cookie( "sptoken", token );
                    cookie.setMaxAge( 1800 );
                    response.addCookie( cookie );
                }
                else
                {
                    request.setAttribute( "error", "Wrong Username or Password !!!" );
                    response.getWriter().write( "Error, Wrong Username or Password" );
                    response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                }
            }
            catch ( NamingException e )
            {
                request.setAttribute( "error", "karaf exceptions !!!" );
                response.getWriter().write( "Error, karaf exceptions !!!" );
                response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
            }
        }
        else
        {
            request.setAttribute( "error", "Please enter username or password" );
            response.getWriter().write( "Error, Please enter username or password" );
            response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
        }
    }
}