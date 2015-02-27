package org.safehaus.subutai.server.ui.util;


import org.safehaus.subutai.common.security.SubutaiLoginContext;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;


/**
 * Vaadin utils
 */
public abstract class SubutaiVaadinUtils
{

    public static SubutaiLoginContext getSubutaiLoginContext()
    {
        VaadinRequest request = VaadinService.getCurrentRequest();
        return request != null ? ( SubutaiLoginContext ) request.getWrappedSession().getAttribute(
                SubutaiLoginContext.SUBUTAI_LOGIN_CONTEXT_NAME ) : new SubutaiLoginContext(  );
    }
}
