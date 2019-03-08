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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import io.subutai.common.settings.Common;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;


public class IndexFilter implements Filter
{

    private final Logger log = LoggerFactory.getLogger( getClass() );


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

            if ( "".equals( url ) || "/".equals( url ) )
            {
                RequestDispatcher view = servletRequest.getRequestDispatcher( "index.html" );
                HttpServletResponse response = ( HttpServletResponse ) servletResponse;
                boolean isCookieSet = isCookieSet( ( HttpServletRequest ) servletRequest,
                        Common.E2E_PLUGIN_USER_KEY_FINGERPRINT_NAME );
                try
                {
                    IdentityManager identityManager = ServiceLocator.getServiceOrNull( IdentityManager.class );
                    if ( !isCookieSet && identityManager != null )
                    {
                        //identityManager.getActiveUser() returns always null here
                        User user = identityManager.getUserByKeyId( identityManager.getPeerOwnerId() );

                        if ( StringUtils.isBlank( user.getFingerprint() ) )
                        {
                            throw new IllegalStateException( "No Peer owner is set yet..." );
                        }

                        setCookie( response, Common.E2E_PLUGIN_USER_KEY_FINGERPRINT_NAME, user.getFingerprint() );
                    }
                }
                catch ( Exception ex )
                {
                    if ( !isCookieSet )
                    {
                        setCookie( response, Common.E2E_PLUGIN_USER_KEY_FINGERPRINT_NAME, "no owner" );
                    }
                }

                view.forward( servletRequest, response );

                return;
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
                    log.error( "Error redirecting {}", e.getMessage() );
                }
            }
            else
            {
                filterChain.doFilter( servletRequest, servletResponse );
            }
        }
    }


    private void setCookie( HttpServletResponse response, String cookieName, String cookieValue )
    {
        Cookie fingerprint = new Cookie( cookieName, cookieValue );
        fingerprint.setSecure( true );
        response.addCookie( fingerprint );
    }


    private boolean isCookieSet( HttpServletRequest request, String cookieName )
    {
        if ( request.getCookies() == null )
        {
            return false;
        }

        for ( Cookie cookie : request.getCookies() )
        {
            if ( cookie.getName().equalsIgnoreCase( cookieName ) )
            {
                return true;
            }
        }

        return false;
    }


    @Override
    public void destroy()
    {

    }
}
