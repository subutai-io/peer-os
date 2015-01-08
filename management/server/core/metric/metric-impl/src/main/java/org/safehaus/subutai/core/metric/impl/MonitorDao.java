package org.safehaus.subutai.core.metric.impl;


import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.core.metric.impl.dao.SubscriberDataService;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Monitor DAO
 */
public class MonitorDao
{
    private static final String INVALID_ENV_ID = "Invalid environment id";
    protected DbUtil dbUtil;
    private SubscriberDataService dataService;


<<<<<<< HEAD

    public MonitorDao( final DataSource dataSource ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.dbUtil = new DbUtil( dataSource );

        setupDb();
    }


    public MonitorDao( EntityManager em ) throws DaoException
=======
    public MonitorDao( EntityManagerFactory emf ) throws DaoException
>>>>>>> master
    {
        this.dataService = new SubscriberDataService( em );
    }


   public void addSubscription( UUID environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkNotNull( environmentId, INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );

<<<<<<< HEAD
        dataService.update( environmentId.toString(), subscriberId );
=======

        dataService.update( environmentId.toString(), subscriberId );

>>>>>>> master
    }


    public void removeSubscription( UUID environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkNotNull( environmentId, INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );

<<<<<<< HEAD
=======

>>>>>>> master
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
