package io.subutai.server.ui.web.controller;



import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;


/**
 * Servlet implementation class UserController
 */
public class UserController extends HttpServlet
{
    private static final long serialVersionUID = 1L;


    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserController()
    {
        super();
    }


    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
            IOException
    {
        doPost( request, response );
    }


    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
            IOException
    {
        try
        {
            String username = request.getParameter( "username" );
            String password = request.getParameter( "password" );

            if( !Strings.isNullOrEmpty(username))
            {

                IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
                User user = identityManager.authenticateUser( username, password  );

                if(user == null)
                {
                    request.getSession().setAttribute( "userSessionData", user);
                    response.sendRedirect( "vui" );
                }
                else
                {
                    request.setAttribute( "error" ,"Invalid Username or Password !!!" );
                    request.getRequestDispatcher( "login.jsp"  ).forward( request,response );
                }
            }
            else
            {
                request.setAttribute( "error" ,"Please enter username or password !!!" );
                request.getRequestDispatcher( "login.jsp"  ).forward( request,response );
            }

        }
        catch ( Exception ex )
        {
            //request.setAttribute( "error", ex.toString() );
            //request.getRequestDispatcher( "error.jsp" ).forward( request, response );
        }

    }
}