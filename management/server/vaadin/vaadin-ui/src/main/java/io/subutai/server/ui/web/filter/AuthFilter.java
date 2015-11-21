package io.subutai.server.ui.web.filter;


import java.io.IOException;
import java.security.PrivilegedAction;
import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;


public class AuthFilter implements Filter
{

    private static final Logger LOG = LoggerFactory.getLogger( AuthFilter.class.getName() );


    /* ****************************************************************
     *
     */
    @Override
    public void init( FilterConfig config ) throws ServletException
    {
        //
    }
    /* ****************************************************************
     *
     */


    @Override
    public void destroy()
    {
        //
    }


    /* ****************************************************************
     *
     */
    @Override
    public void doFilter( final ServletRequest sRequest, final ServletResponse sResponse, final FilterChain chain )
            throws ServletException, IOException
    {
        HttpServletRequest request = ( HttpServletRequest ) sRequest;


        if ( checkURL( request ) )
        {
            if ( request.getSession().getAttribute( "userSessionData" ) != null )
            {
                Session userSession = ( Session ) request.getSession().getAttribute( "userSessionData" );


                //*******************************************************************
                Subject.doAs( userSession.getSubject(), new PrivilegedAction<Void>()
                {
                    @Override
                    public Void run()
                    {
                        //*********Continue Chain Authorized *************
                        try
                        {
                            chain.doFilter( sRequest, sResponse );
                        }
                        catch ( Exception e )
                        {
                            LOG.error( "**** Error in Do Chain:" + e.toString() );
                        }
                        //************************************************
                        return null;
                    }
                } );
                //*******************************************************************
            }
            else
            {
                request.getRequestDispatcher( "login.jsp" ).forward( sRequest, sResponse );
            }
        }
        else
        {
            chain.doFilter( sRequest, sResponse );
        }
    }


    /* ****************************************************************
     *
     */
    private synchronized boolean checkURL( HttpServletRequest request )
    {
        String requestURI = request.getRequestURI();

        try
        {

            if ( !requestURI.startsWith( "/subutai/usercontrol" ) &&
                    !requestURI.startsWith( "/subutai/login.jsp" ) &&
                    !requestURI.startsWith( "/subutai/error.jsp" ) &&
                    !requestURI.startsWith( "/subutai/index.jsp" ) &&
                    !requestURI.startsWith( "/subutai/vui/VAADIN" ) &&
                    !requestURI.startsWith( "/subutai/vui/HEARTBEAT" ) &&
                    !requestURI.startsWith( "/subutai/vui/APP/connector" ) &&
                    !requestURI.startsWith( "/subutai/vui/UIDL" ) &&
                    !requestURI.startsWith( "/subutai/VAADIN" ) &&
                    !requestURI.startsWith( "/subutai/resources" ) )
            {
                return true;
            }
            else
            {
                //********Bypass POLLIng requests ******************************
                if ( requestURI.startsWith( "/subutai/vui/UIDL" ) )
                {
                    //String requestBody = "";
                    //        request.getReader().lines().collect( Collectors.joining( System.lineSeparator() ) );
                    return true;
                }
                //*****************************************************************
            }
        }
        catch ( Exception e )
        {
            return false;
        }

        return false;
    }
}