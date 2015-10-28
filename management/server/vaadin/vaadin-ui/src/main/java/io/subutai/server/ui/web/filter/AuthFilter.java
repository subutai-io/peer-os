package io.subutai.server.ui.web.filter;


import java.io.IOException;
import java.security.PrivilegedAction;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;


public class AuthFilter implements Filter
{
    private FilterChain chain;
    private ServletRequest req;
    private ServletResponse res;


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
        String requestURI = request.getRequestURI();

        if ( !requestURI.startsWith( "/subutai/usercontroller" ) &&
                !requestURI.startsWith( "/subutai/login.jsp" ) &&
                !requestURI.startsWith( "/subutai/index.jsp" ) &&
                !requestURI.startsWith( "/subutai/VAADIN" ) &&
                !requestURI.startsWith( "/subutai/resources" ) )
        {
            if ( request.getSession().getAttribute( "userSession" ) != null )
            {
                this.chain = chain;
                this.req = req;
                this.res = res;

                User user = ( User ) request.getSession().getAttribute( "userSession" );
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
    private void login( String userName, String password )
    {
        try
        {
            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
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
        }
    }


    @Override
    public void destroy()
    {
        //
    }

}