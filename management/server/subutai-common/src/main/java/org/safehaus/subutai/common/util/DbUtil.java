package org.safehaus.subutai.common.util;


import com.google.common.base.Preconditions;
import com.sun.rowset.CachedRowSetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Database utilities
 */
public class DbUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( DbUtil.class.getName() );

    private final DataSource dataSource;


    public DbUtil( DataSource dataSource )
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.dataSource = dataSource;
    }


    public ResultSet select( String sql, Object... params ) throws SQLException
    {
        return select( dataSource, sql, params );
    }


    public Integer update( String sql, Object... params ) throws SQLException
    {
        return update( dataSource, sql, params );
    }


    public static ResultSet select( DataSource dataSource, String sql, Object... params ) throws SQLException
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
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
        catch ( Exception ex )
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
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement( sql );
            short i = 0;
            for ( Object par : params )
            {
                if ( par instanceof Reader )
                {
                    ps.setCharacterStream( ++i, ( Reader ) par );
                }
                else
                {
                    ps.setObject( ++i, par );
                }
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
