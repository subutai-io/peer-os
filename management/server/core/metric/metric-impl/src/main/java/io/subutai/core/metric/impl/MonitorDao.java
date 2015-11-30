package io.subutai.core.metric.impl;


import java.util.Set;

import javax.persistence.EntityManagerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.exception.DaoException;
import io.subutai.common.peer.EnvironmentId;
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


    public void addSubscription( EnvironmentId environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkNotNull( environmentId , INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId.getId() ), INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );


        dataService.update( environmentId.getId(), subscriberId );
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
