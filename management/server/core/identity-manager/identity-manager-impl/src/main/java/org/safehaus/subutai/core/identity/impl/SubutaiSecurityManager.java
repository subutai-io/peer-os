package org.safehaus.subutai.core.identity.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;


/**
 * Created by timur on 1/27/15.
 */
public class SubutaiSecurityManager extends DefaultSecurityManager
{
    Logger LOG = LoggerFactory.getLogger( SubutaiSecurityManager.class );


    @Override
    public Subject login( final Subject subject, final AuthenticationToken token ) throws AuthenticationException
    {
        LOG.debug( String.format( "Thread ID: %d", Thread.currentThread().getId() ) );
        String loginPrincipal = ( String ) token.getPrincipal();
        DefaultSessionManager sm = ( DefaultSessionManager ) getSessionManager();
        for ( Session session : sm.getSessionDAO().getActiveSessions() )
        {
            SimplePrincipalCollection p =
                    ( SimplePrincipalCollection ) session.getAttribute( DefaultSubjectContext.PRINCIPALS_SESSION_KEY );

            Object primaryPrincipal = p != null ? p.getPrimaryPrincipal() : "NULL";
            LOG.debug( String.format( "%s %s", session.getId(), primaryPrincipal ) );

            //            if ( p != null && loginPrincipal.equals( primaryPrincipal ) )
            //            {
            //                throw new AuthenticationException( "Principal already authenticated." );
            //            }
        }
        return super.login( subject, token );
    }


}
