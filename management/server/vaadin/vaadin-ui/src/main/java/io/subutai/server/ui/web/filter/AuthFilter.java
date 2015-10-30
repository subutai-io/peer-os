package io.subutai.server.ui.web.filter;


import java.io.BufferedReader;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.stream.Collectors;

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

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;


public class AuthFilter implements Filter
{
    private FilterChain chain;
    private ServletRequest req;
    private ServletResponse res;

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
    public void doFilter( final ServletRequest req, final ServletResponse res, final FilterChain chain )
            throws ServletException, IOException
    {
        HttpServletRequest request = ( HttpServletRequest ) req;

        if (checkURL(request))
        {

            if ( request.getSession().getAttribute( "userSessionData" ) != null)
            {
                this.chain = chain;
                this.req = req;
                this.res = res;

                User user = ( User ) request.getSession().getAttribute( "userSessionData" );
                login( user.getUserName(), user.getPassword() );
            }
            else
            {
                req.getRequestDispatcher( "login.jsp" ).forward( req, res );
            }
        }
        else
        {
            chain.doFilter( req, res );
        }
    }


    /* ****************************************************************
     *
     */
    private boolean checkURL(HttpServletRequest request )
    {
        String requestURI = request.getRequestURI();

        LOG.info("****** Req URL:" +request.getRequestURL().toString() );

        if( !requestURI.startsWith( "/subutai/usercontrol" ) &&
            !requestURI.startsWith( "/subutai/login.jsp" ) &&
            !requestURI.startsWith( "/subutai/index.jsp" ) &&
            !requestURI.startsWith( "/subutai/vui/VAADIN" ) &&
            !requestURI.startsWith( "/subutai/vui/HEARTBEAT" ) &&
            !requestURI.startsWith( "/subutai/vui/APP/connector" ) &&
            !requestURI.startsWith( "/subutai/vui/UIDL" ) &&
            !requestURI.startsWith( "/subutai/VAADIN" ) &&
            !requestURI.startsWith( "/subutai/resources" ))
        {
            return true;
        }
        else
        {
            if ( requestURI.startsWith( "/subutai/vui/UIDL" ) )
            {
                //AccessControlContext acc = AccessController.getContext();
                //Subject.

                Enumeration<String> pnames = request.getParameterNames();

                while(pnames.hasMoreElements())
                {
                    String par = (String)pnames.nextElement();


                    try
                    {
                        BufferedReader buff = request.getReader();
                        String test = request.getReader().lines().collect( Collectors.joining( System.lineSeparator() ));
                        LOG.info("****** Parameters :" +par);
                        LOG.info("****** Parameters Value:" +request.getParameter( par ) );
                        LOG.info("****** Parameters Body:" +test );

                    }
                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }

                }

                return false;
            }
            else
            {
                return false;
            }
        }
    }

    /* ****************************************************************
     *
     */
    private void login( String userName, String password )
    {
        try
        {
            IdentityManager identityManager = ServiceLocator.getServiceNoCache ( IdentityManager.class );
            Subject subject = identityManager.login( userName, password );
            Subject.doAs( subject, new PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    if ( chain != null )
                    {
                        //*********Continue Chain Auhtorized *************
                        try
                        {
                            chain.doFilter( req, res );
                        }
                        catch ( Exception e )
                        {
                        }
                        //************************************************
                    }
                    return null;
                }
            } );
    }
        catch ( Exception e )
        {
            LOG.error( "**** Error in Servlet Filter:" + e.toString() );
        }
    }


    @Override
    public void destroy()
    {
        //
    }

}