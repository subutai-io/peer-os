package io.subutai.webui;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;


public class IndexFilter implements Filter
{


    @Override
    public void init( final FilterConfig filterConfig ) throws ServletException
    {
    }


    @Override
    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
            throws IOException, ServletException
    {
        if ( servletRequest instanceof HttpServletRequest )
        {
            String url = ( ( HttpServletRequest ) servletRequest ).getRequestURI();

            if ( url.equals( "" ) || url.equals( "/" ) )
            {
                RequestDispatcher view = servletRequest.getRequestDispatcher( "index.html" );
                HttpServletResponse response = ( HttpServletResponse ) servletResponse;
                try
                {
                    IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
                    if ( identityManager != null )
                    {
                        User user = identityManager.getUserByKeyId( identityManager.getPeerOwnerId() );
                        if ( Strings.isNullOrEmpty( user.getFingerprint() ) )
                        {
                            throw new IllegalStateException( "No Peer owner is set yet..." );
                        }
                        Cookie fingerprint = new Cookie( "su_fingerprint", user.getFingerprint() );
                        fingerprint.setSecure( true );
                        response.addCookie( fingerprint );
                    }
                }
                catch ( Exception ex )
                {
                    Cookie fingerprint = new Cookie( "su_fingerprint", "no owner" );
                    fingerprint.setSecure( true );
                    response.addCookie( fingerprint );
                }
                view.forward( servletRequest, response );
            }
            if ( !( url.startsWith( "/rest" ) || url.startsWith( "/subutai" ) || url.startsWith( "/fav" ) || url
                    .startsWith( "/plugin" ) || url.startsWith( "/assets" ) || url.startsWith( "/css" ) || url
                    .startsWith( "/fonts" ) || url.startsWith( "/scripts" ) || url.startsWith( "/login" ) ) && !url
                    .contains( "#" ) )
            {
                try
                {
                    ( ( HttpServletResponse ) servletResponse ).sendRedirect( "/#" + url );
                }
                catch ( Exception e )
                {
                    // precation to possible exceptions
                }
            }
            else
            {
                filterChain.doFilter( servletRequest, servletResponse );
            }
        }
    }


    @Override
    public void destroy()
    {

    }
}
