package io.subutai.core.metric.impl;


import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;

import org.safehaus.subutai.common.exception.DaoException;
import io.subutai.core.metric.impl.dao.SubscriberDataService;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


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


    public void addSubscription( UUID environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkNotNull( environmentId, INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );


        dataService.update( environmentId.toString(), subscriberId );
    }


    public void removeSubscription( UUID environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkNotNull( environmentId, INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );


        dataService.remove( environmentId.toString(), subscriberId );
    }


    public Set<String> getEnvironmentSubscribersIds( UUID environmentId ) throws DaoException
    {
        Preconditions.checkNotNull( environmentId, INVALID_ENV_ID );
        Set<String> subscribersIds;
        subscribersIds = dataService.getEnvironmentSubscriberIds( environmentId.toString() );

        return subscribersIds;
    }
}
