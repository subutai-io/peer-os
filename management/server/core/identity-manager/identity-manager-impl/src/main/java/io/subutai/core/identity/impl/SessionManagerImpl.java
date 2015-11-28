package io.subutai.core.identity.impl;


import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.PermitAll;

import org.apache.commons.lang.time.DateUtils;

import com.google.common.collect.Maps;

import io.subutai.core.identity.api.SessionManager;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.impl.model.SessionEntity;


/**
 * Implementation of SessionManager
 */
public class SessionManagerImpl implements SessionManager
{
    //Session Expiration time in mins
    //****************************************
    private static int SESSION_TIMEOUT = 30;

    //****************************************
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Map<String, Session> sessionContext = Maps.newTreeMap();
    private IdentityDataService identityDataService = null;
    //****************************************


    //*****************************************
    public SessionManagerImpl(IdentityDataService identityDataService)
    {
        this.identityDataService = identityDataService;
    }


    //*****************************************
    @Override
    public void startSessionController()
    {
        executorService.scheduleWithFixedDelay( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    removeInvalidTokens();
                    invalidateSessions();
                }
                catch(Exception ex)
                {
                }
            }
        }, 10, 10, TimeUnit.MINUTES );
    }


    //*****************************************
    @Override
    public void stopSessionController()
    {
        if ( executorService != null )
        {
            executorService.shutdown();
        }
    }


    /* *************************************************
     */
    @Override
    public Session startSession( String sessionId, Session userSession, User user )
    {
        try
        {
            if ( userSession == null )
            {
                userSession = new SessionEntity();
                userSession.setUser( user );
                userSession.setStatus( 1 );
                userSession.setStartDate( new Date( System.currentTimeMillis() ) );
                sessionContext.put( sessionId, userSession );
            }
            else
            {
                extendSessionTime( userSession );
            }
        }

        catch ( Exception ex )
        {
        }

        return userSession;
    }


    /* *************************************************
     *
     */
    @Override
    public Session getValidSession( String sessionId)
    {
        Session sc = sessionContext.get( sessionId );

        if ( sc != null )
            return sc;
        else
            return null;
    }


    /* *************************************************
     *
     */
    @Override
    public void extendSessionTime( Session userSession )
    {
        Date currentDate = new Date( System.currentTimeMillis() );
        userSession.setStartDate( DateUtils.addMinutes( currentDate, SESSION_TIMEOUT ) );
    }


    /* *************************************************
     *
     */
    @Override
    public void extendSessionTime( String sessionId )
    {
        Session sc = sessionContext.get( sessionId );

        if(sc!=null)
        {
            extendSessionTime( sc );
        }
    }


    /* *************************************************
     *
     */
    @Override
    public void endSession( String sessionId )
    {
        try
        {
            sessionContext.remove( sessionId );
        }
        catch ( Exception ex )
        {
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void invalidateSessions()
    {
        Date currentDate = DateUtils.addMinutes( new Date( System.currentTimeMillis() ), -SESSION_TIMEOUT );

        for ( Iterator<Session> iterator = sessionContext.values().iterator(); iterator.hasNext(); )
        {
            final Session session = iterator.next();
            if ( session.getStartDate().before( currentDate ) )
            {
                iterator.remove();
            }
        }
    }


    /* *************************************************
     */
    private void removeInvalidTokens()
    {
        identityDataService.removeInvalidTokens();
    }


    //*****************************************
    @Override
    public int getSessionTimeout()
    {
        return SESSION_TIMEOUT;
    }


    //*****************************************
    public static void setSessionTimeout( final int sessionTimeout )
    {
        SESSION_TIMEOUT = sessionTimeout;
    }


}
