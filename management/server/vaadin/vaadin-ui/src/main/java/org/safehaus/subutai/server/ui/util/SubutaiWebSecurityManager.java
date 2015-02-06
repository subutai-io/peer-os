package org.safehaus.subutai.server.ui.util;


import org.safehaus.subutai.common.helper.UserIdMdcHelper;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;


public class SubutaiWebSecurityManager extends DefaultWebSecurityManager
{
//    public SubutaiWebSecurityManager()
    //    {
    //
    //    }


    /**
     * After login set the userId MDC attribute.
     */
    @Override
    public Subject login( Subject subject, final AuthenticationToken token ) throws AuthenticationException
    {
        try
        {
            subject = super.login( subject, token );
            boolean b = UserIdMdcHelper.isSet();
            String a = UserIdMdcHelper.get();
            return subject;
        }
        catch ( AuthenticationException e )
        {
            throw e;
        }
    }


    /**
     * After logout unset the userId MDC attribute.
     */
    @Override
    public void logout( final Subject subject )
    {
        super.logout( subject );
        UserIdMdcHelper.unset();
    }
}
