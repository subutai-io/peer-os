package io.subutai.core.metric.impl;


import java.util.Set;

import javax.persistence.EntityManagerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.exception.DaoException;
import io.subutai.core.metric.impl.dao.SubscriberDao;


/**
 * Monitor DAO
 */
public class MonitorDataService
{
    private static final String INVALID_ENV_ID = "Invalid environment id";
    private SubscriberDao subscriberDao;


    public MonitorDataService( EntityManagerFactory emf ) throws DaoException
    {
        this.subscriberDao = new SubscriberDao( emf );
    }


    public void addSubscription( String environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkNotNull( environmentId , INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );


        subscriberDao.update( environmentId, subscriberId );
    }


    public void removeSubscription( String environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );


        subscriberDao.remove( environmentId, subscriberId );
    }


    public Set<String> findHandlersByEnvironment( String environmentId ) throws DaoException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), INVALID_ENV_ID );
        return subscriberDao.findHandlersByEnvironment( environmentId );
    }
}
