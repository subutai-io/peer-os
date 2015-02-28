package org.safehaus.subutai.server.ui.util;


import org.safehaus.subutai.common.security.NullSubutaiLoginContext;
import org.safehaus.subutai.common.security.SubutaiLoginContext;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;


/**
 * Vaadin utils for Subutai project
 */
public abstract class SubutaiVaadinUtils
{

    public static SubutaiLoginContext getSubutaiLoginContext()
    {
        VaadinRequest request = VaadinService.getCurrentRequest();
        SubutaiLoginContext loginContext = NullSubutaiLoginContext.getInstance();

        if ( request != null
                && request.getWrappedSession().getAttribute( SubutaiLoginContext.SUBUTAI_LOGIN_CONTEXT_NAME ) != null )
        {
            loginContext = ( SubutaiLoginContext ) request.getWrappedSession().getAttribute(
                    SubutaiLoginContext.SUBUTAI_LOGIN_CONTEXT_NAME );
        }
        return loginContext;
    }
}
