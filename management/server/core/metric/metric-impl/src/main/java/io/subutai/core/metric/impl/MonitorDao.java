package io.subutai.core.metric.impl;


import java.util.Set;

import javax.persistence.EntityManagerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.exception.DaoException;
import io.subutai.core.metric.impl.dao.SubscriberDataService;


/**
 * Monitor DAO
 */
public class MonitorDao
{
    private static final String INVALID_ENV_ID = "Invalid environment id";
    private SubscriberDataService dataService;


    public MonitorDao( EntityManagerFactory emf ) throws DaoException
    {
        this.dataService = new SubscriberDataService( emf );
    }


    public void addSubscription( String environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );


        dataService.update( environmentId, subscriberId );
    }


    public void removeSubscription( String environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );


        dataService.remove( environmentId, subscriberId );
    }


    public Set<String> getEnvironmentSubscribersIds( String environmentId ) throws DaoException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), INVALID_ENV_ID );
        Set<String> subscribersIds;
        subscribersIds = dataService.getEnvironmentSubscriberIds( environmentId );

        return subscribersIds;
    }
}
