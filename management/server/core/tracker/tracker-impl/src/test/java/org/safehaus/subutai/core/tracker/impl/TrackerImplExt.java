package org.safehaus.subutai.core.tracker.impl;


import java.sql.SQLException;

import javax.sql.DataSource;

import org.safehaus.subutai.common.util.DbUtil;


/**
 * Class extending TrackerImpl for testing purposes
 */
public class TrackerImplExt extends TrackerImpl
{
    public TrackerImplExt( final DataSource dataSource, DbUtil dbUtil ) throws SQLException
    {
        //        super( dataSource );
        this.dbUtil = dbUtil;
    }


    @Override
    protected void setupDb()
    {
        //leave this empty to avoid call to real dbutil
    }
}
