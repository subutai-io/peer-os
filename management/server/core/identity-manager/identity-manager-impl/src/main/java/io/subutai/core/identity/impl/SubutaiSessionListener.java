package io.subutai.core.identity.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;


/**
 * Subutai session listener.
 */
public class SubutaiSessionListener implements SessionListener
{
    private static final Logger LOG = LoggerFactory.getLogger( SubutaiSessionListener.class );


    @Override
    public void onStart( final Session session )
    {
        LOG.debug( String.format( "Shiro session started: %s. Timeout: %d. %s", session.getId(), session.getTimeout(),
                session ) );
    }


    @Override
    public void onStop( final Session session )
    {
        LOG.debug( String.format( "Shiro session stopped: %s. Timeout: %d", session.getId(), session.getTimeout() ) );
    }


    @Override
    public void onExpiration( final Session session )
    {
        LOG.debug( String.format( "Shiro session expired: %s. Timeout: %d", session.getId(), session.getTimeout() ) );
    }
}
