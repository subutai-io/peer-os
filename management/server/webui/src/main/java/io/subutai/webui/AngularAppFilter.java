package io.subutai.webui;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


public class AngularAppFilter implements Filter
{
    private FilterConfig filterConfig;

    @Override
    public void init( final FilterConfig filterConfig ) throws ServletException
    {
        this.filterConfig = filterConfig;
    }


    @Override
    public void doFilter( final ServletRequest servletRequest, final ServletResponse servletResponse,
                          final FilterChain filterChain ) throws IOException, ServletException
    {
        System.out.println("test");
    }


    @Override
    public void destroy()
    {

    }
}
