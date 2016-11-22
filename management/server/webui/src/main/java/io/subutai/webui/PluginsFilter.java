package io.subutai.webui;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import io.subutai.common.util.ServiceLocator;
import io.subutai.webui.api.WebuiModuleService;


public class PluginsFilter implements Filter
{
    private FilterConfig filterConfig;
    private WebuiModuleService webuiModuleService;


    @Override
    public void init( final FilterConfig filterConfig ) throws ServletException
    {
        this.filterConfig = filterConfig;
    }


    @Override
    public void doFilter( final ServletRequest servletRequest, final ServletResponse servletResponse,
                          final FilterChain filterChain ) throws IOException, ServletException
    {

        webuiModuleService = ServiceLocator.lookup( WebuiModuleService.class );

        servletResponse.getWriter().write( webuiModuleService.getModulesListJson() );
    }


    @Override
    public void destroy()
    {

    }
}
