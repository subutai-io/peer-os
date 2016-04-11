package io.subutai.webui;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class IndexFilter implements Filter
{
    private FilterConfig filterConfig;


    @Override
    public void init( final FilterConfig filterConfig ) throws ServletException
    {
        this.filterConfig = filterConfig;
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
                view.forward( servletRequest, servletResponse );
            }
            if ( !( url.startsWith( "/rest" ) || url.startsWith( "/subutai" ) ||
                    url.startsWith( "/fav" ) || url.startsWith( "/plugin" ) ||
                    url.startsWith( "/assets" ) || url.startsWith( "/css" ) ||
                    url.startsWith( "/fonts" ) || url.startsWith( "/scripts" ) ||
                    url.startsWith( "/login" ) ) && !url.contains( "#" ) )
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
