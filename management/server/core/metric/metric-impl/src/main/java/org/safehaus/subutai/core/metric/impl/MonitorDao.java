package org.safehaus.subutai.core.metric.impl;


import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;
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



    public MonitorDao( final DataSource dataSource ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.dbUtil = new DbUtil( dataSource );

        setupDb();
    }


    public MonitorDao( EntityManagerFactory emf ) throws DaoException
    {
        this.dataService = new SubscriberDataService( emf );
    }


    protected void setupDb() throws DaoException
    {
        String sql = "create table if not exists monitor_subscriptions(environmentId uuid, subscriberId varchar(100), "
                + " PRIMARY KEY (environmentId, subscriberId));";

        try
        {
            dbUtil.update( sql );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    public void addSubscription( UUID environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkNotNull( environmentId, INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );

        //        try
        //        {
        dataService.update( environmentId.toString(), subscriberId );
        //            dbUtil.update( "merge into monitor_subscriptions(environmentId, subscriberId) values(?,?)",
        // environmentId,
        //                    subscriberId );
        //        }
        //        catch ( SQLException e )
        //        {
        //            throw new DaoException( e );
        //        }
    }


    public void removeSubscription( UUID environmentId, String subscriberId ) throws DaoException
    {

        Preconditions.checkNotNull( environmentId, INVALID_ENV_ID );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), "Invalid subscriber id" );

        //        try
        //        {
        dataService.remove( environmentId.toString(), subscriberId );
        //            dbUtil.update( "delete from monitor_subscriptions where environmentId = ? and subscriberId = ?",
        //                    environmentId, subscriberId );
        //        }
        //        catch ( SQLException e )
        //        {
        //            throw new DaoException( e );
        //        }
    }


    public Set<String> getEnvironmentSubscribersIds( UUID environmentId ) throws DaoException
    {
        Preconditions.checkNotNull( environmentId, INVALID_ENV_ID );
        Set<String> subscribersIds;
        subscribersIds = dataService.getEnvironmentSubscriberIds( environmentId.toString() );
        //        try
        //        {
        //            ResultSet rs = dbUtil.select( "select subscriberId from monitor_subscriptions where
        // environmentId = ?",
        //                    environmentId );
        //            if ( rs != null )
        //            {
        //                while ( rs.next() )
        //                {
        //                    subscribersIds.add( rs.getString( "subscriberId" ) );
        //                }
        //            }
        //        }
        //        catch ( SQLException e )
        //        {
        //            throw new DaoException( e );
        //        }
        return subscribersIds;
    }
}
