package io.subutai.webui;

import io.subutai.webui.api.WebuiModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IndexFilter implements Filter
{
    private FilterConfig filterConfig;
    private WebuiModuleService webuiModuleService;

    private static final Logger LOG = LoggerFactory.getLogger( AngularAppFilter.class.getName() );

    @Override
    public void init( final FilterConfig filterConfig ) throws ServletException
    {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        if (servletRequest instanceof HttpServletRequest) {
            String url = ((HttpServletRequest)servletRequest).getRequestURI().toString();

            if( url == null || url.equals("") || url.equals("/") )
            {
                RequestDispatcher view = servletRequest.getRequestDispatcher("index.html");
                view.forward(servletRequest, servletResponse);
            }
            if( !(url.startsWith("/rest") || url.startsWith("/subutai") ||
                    url.startsWith("/fav") || url.startsWith("/plugin") ||
                    url.startsWith("/assets") || url.startsWith("/css") ||
                    url.startsWith("/fonts") || url.startsWith("/scripts") ||
                    url.startsWith("/login")) && !url.contains("#")  )
            {
                try
                {
                    ((HttpServletResponse)servletResponse).sendRedirect("/#" + url);
                }
                catch (Exception e)
                {
                    // precation to possible exceptions
                }
            }
            else
            {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        }
    }

    @Override
    public void destroy()
    {

    }
}
