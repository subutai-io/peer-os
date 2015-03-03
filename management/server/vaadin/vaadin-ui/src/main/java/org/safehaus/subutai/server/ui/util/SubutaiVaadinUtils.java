package org.safehaus.subutai.server.ui.util;


import javax.servlet.http.Cookie;

import org.safehaus.subutai.common.security.NullSubutaiLoginContext;
import org.safehaus.subutai.common.security.SubutaiLoginContext;
import org.safehaus.subutai.common.util.JsonUtil;

import com.google.common.base.Strings;
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

        if ( request != null )
        {
            if ( request.getWrappedSession().getAttribute( SubutaiLoginContext.SUBUTAI_LOGIN_CONTEXT_NAME ) != null )
            {
                loginContext = ( SubutaiLoginContext ) request.getWrappedSession().getAttribute(
                        SubutaiLoginContext.SUBUTAI_LOGIN_CONTEXT_NAME );
            }
            else
            {
                for ( Cookie cookie : request.getCookies() )
                {
                    if ( cookie.getName().equals( SubutaiLoginContext.SUBUTAI_LOGIN_CONTEXT_NAME ) && !Strings
                            .isNullOrEmpty( cookie.getValue() ) )
                    {
                        loginContext = JsonUtil.fromJson( cookie.getValue(), SubutaiLoginContext.class );
                        break;
                    }
                }
            }
        }


        return loginContext;
    }
}
