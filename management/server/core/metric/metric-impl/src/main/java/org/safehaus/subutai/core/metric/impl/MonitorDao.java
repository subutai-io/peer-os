package org.safehaus.subutai.core.metric.impl;


import java.sql.SQLException;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.DbUtil;

import com.google.common.base.Preconditions;


/**
 * Monitor DAO
 */
public class MonitorDao
{
    protected DbUtil dbUtil;


    public MonitorDao( final DataSource dataSource ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.dbUtil = new DbUtil( dataSource );

        setupDb();
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
}
