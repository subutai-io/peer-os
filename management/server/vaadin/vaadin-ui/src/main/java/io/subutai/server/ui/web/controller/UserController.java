package io.subutai.server.ui.web.controller;



import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
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
                Session userSession = identityManager.login( username, password  );

                if(userSession != null)
                {
                    Random r = new Random();
                    request.getSession().setAttribute( "userSessionData", userSession);
                    response.sendRedirect( "vui/?" + r.nextInt(100));
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
        }

    }
}