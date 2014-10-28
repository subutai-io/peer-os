package org.safehaus.subutai.core.message.impl;


import java.io.StringReader;
import java.lang.reflect.Type;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.message.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;


/**
 * Queue DAO
 */
public class QueueDao
{
    private static final Logger LOG = LoggerFactory.getLogger( QueueDao.class.getName() );

    protected DbUtil dbUtil;


    public QueueDao( final DataSource dataSource ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.dbUtil = new DbUtil( dataSource );

        setupDb();
    }


    protected void setupDb() throws DaoException
    {

        String sql =
                "create table if not exists message_queue( messageId uuid, targetPeerId uuid, recipient varchar(50),"
                        + " timeToLive int, attempts smallint, payload clob, PRIMARY KEY (messageId));";
        try
        {
            dbUtil.update( sql );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    public void saveMessage( Envelope envelope ) throws DaoException
    {
        Type messageType = new TypeToken<Message>()
        {

        }.getType();
        try
        {
            dbUtil.update( "insert into message_queue( messageId, targetPeerId, recipient, timeToLive, attempts, " +
                            "payload) values" + "(?,?,?,?,?,?)", envelope.getMessage().getId(),
                    envelope.getTargetPeerId(), envelope.getRecipient(), envelope.getTimeToLive(), 0,
                    new StringReader( JsonUtil.toJson( envelope.getMessage(), messageType ) ) );
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in saveMessage", e );
            throw new DaoException( e );
        }
    }
}
