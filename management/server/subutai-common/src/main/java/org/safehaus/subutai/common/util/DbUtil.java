package org.safehaus.subutai.common.util;


import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.rowset.CachedRowSetImpl;


/**
 * Database utilities
 */
public class DbUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( DbUtil.class.getName() );


    private DbUtil()
    {
    }


    public static java.sql.ResultSet select( DataSource dataSource, String sql, Object... params ) throws SQLException
    {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement( sql );
            short i = 0;
            for ( Object par : params )
            {
                ps.setObject( ++i, par );
            }
            rs = ps.executeQuery();
            CachedRowSetImpl cachedRs = new CachedRowSetImpl();
            cachedRs.populate( rs );
            return cachedRs;
        }
        catch ( SQLException ex )
        {
            LOG.error( "Error in select", ex );
            throw ex;
        }
        finally
        {
            if ( rs != null )
            {
                try
                {
                    rs.close();
                }
                catch ( SQLException e )
                {
                    LOG.warn( "Error closing result set", e );
                }
            }
            if ( ps != null )
            {
                try
                {
                    ps.close();
                }
                catch ( SQLException e )
                {
                    LOG.warn( "Error closing prepared statement", e );
                }
            }
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch ( SQLException e )
                {
                    LOG.warn( "Error closing connection", e );
                }
            }
        }
    }


    public static Integer update( DataSource dataSource, String sql, Object... params ) throws SQLException
    {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement( sql );
            short i = 0;
            for ( Object par : params )
            {
                ps.setObject( ++i, par );
            }
            return ps.executeUpdate();
        }
        catch ( SQLException ex )
        {
            LOG.error( "Error in update", ex );
            throw ex;
        }
        finally
        {
            if ( ps != null )
            {
                try
                {
                    ps.close();
                }
                catch ( SQLException e )
                {
                    LOG.warn( "Error closing prepared statement", e );
                }
            }
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch ( SQLException e )
                {
                    LOG.warn( "Error closing connection", e );
                }
            }
        }
    }
}
