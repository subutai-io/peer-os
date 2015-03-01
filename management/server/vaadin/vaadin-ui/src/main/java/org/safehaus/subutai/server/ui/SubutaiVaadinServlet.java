package org.safehaus.subutai.server.ui;


import org.safehaus.subutai.common.security.SubutaiThreadContext;
import org.safehaus.subutai.server.ui.util.SubutaiVaadinUtils;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;


/**
 * Subutai vaadin servlet
 */
public class SubutaiVaadinServlet extends VaadinServlet
{
    @Override
    protected VaadinServletService createServletService( DeploymentConfiguration deploymentConfiguration )
            throws ServiceException
    {
        VaadinServletService servletService = new VaadinServletService( this, deploymentConfiguration )
        {

            @Override
            public void requestStart( VaadinRequest request, VaadinResponse response )
            {
                super.requestStart( request, response );

                SubutaiThreadContext.set( SubutaiVaadinUtils.getSubutaiLoginContext() );
            }


            @Override
            public void requestEnd( VaadinRequest request, VaadinResponse response, VaadinSession session )
            {
                super.requestEnd( request, response, session );
                SubutaiThreadContext.unset();
            }
        };
        servletService.init();
        return servletService;
    }
}
