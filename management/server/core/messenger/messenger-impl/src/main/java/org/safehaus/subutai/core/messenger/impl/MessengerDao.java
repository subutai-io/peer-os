package org.safehaus.subutai.core.messenger.impl;


import java.io.StringReader;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;


/**
 * Messenger DAO
 */
public class MessengerDao
{
    private static Logger LOG = LoggerFactory.getLogger( MessengerDao.class.getName() );
    private static final int WIDENING_INTERVAL_SEC = 5;
    private static final int MESSAGE_LIMIT_PER_PEER = 10;

    protected DbUtil dbUtil;


    public MessengerDao( final DataSource dataSource ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.dbUtil = new DbUtil( dataSource );

        setupDb();
    }


    protected void setupDb() throws DaoException
    {

        String sql = "SET MAX_LENGTH_INPLACE_LOB 2048; "
                //create table to store taget peers' ids
                + "create table if not exists target_peers ( targetPeerId uuid, PRIMARY KEY(targetPeerId));" +
                //create table for storing outstanding messages
                "create table if not exists messages ( messageId uuid, targetPeerId uuid, " +
                "envelope clob, createDate timestamp default CURRENT_TIMESTAMP(), attempts smallint default 0, " +
                "isSent boolean default false, timeToLive smallint, PRIMARY KEY (messageId) );";
        try
        {
            dbUtil.update( sql );
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in setupDb", e );
            throw new DaoException( e );
        }
    }


    public void purgeExpiredMessages()
    {

        try
        {
            dbUtil.update(
                    //delete message that
                    "delete from messages where " +
                            //are expired (no matter if they are sent or not)
                            "(datediff('SECOND', createDate, CURRENT_TIMESTAMP() ) <= timeToLive " +
                            //and 24 hours passed since expiration
                            "and dateadd('HOUR', 24, dateadd('SECOND', timeToLive, createDate)) < CURRENT_TIMESTAMP()" +
                            ")" );
            //delete target peers that do not have outstanding messages
            dbUtil.update( "delete from target_peers tp where not exists "
                    + "(select 1 from messages m where m.targetPeerId = tp.targetPeerId)" );
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in purgeExpiredMessages", e );
        }
    }


    public Set<Envelope> getEnvelopes()
    {
        Set<Envelope> envelopes = Sets.newHashSet();
        try
        {

            //we need to select target peers separately to allow selection of N messages per each peer within one pass
            //to avoid throttling of other peers in case one peer has message overflow
            ResultSet targetPeersRs = dbUtil.select( "select targetPeerId from target_peers" );
            while ( targetPeersRs != null && targetPeersRs.next() )
            {
                ResultSet messagesRs = dbUtil.select(
                        //select messages where
                        "select envelope, isSent, createDate from messages where " +
                                //widening interval attempt has passed
                                "CURRENT_TIMESTAMP() >= dateadd('SECOND', attempts * ?, createDate) and " +
                                //the message is still not sent
                                "isSent = false and " +
                                //message is not expired
                                "datediff('SECOND', createDate, CURRENT_TIMESTAMP() ) <= timeToLive " +
                                //and belongs to the specified peer
                                "and targetPeerId = ? " +
                                //order by createDate asc to keep message order and limit to 10 messages per peer
                                "order by createDate asc limit ?", WIDENING_INTERVAL_SEC,
                        targetPeersRs.getObject( "targetPeerId" ), MESSAGE_LIMIT_PER_PEER );

                envelopes.addAll( retrieveEnvelopsFromResultSet( messagesRs ) );
            }
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in getEnvelopes", e );
        }
        return envelopes;
    }


    public void markAsSent( Envelope envelope )
    {
        try
        {
            dbUtil.update( "update messages set isSent = true where messageId = ?", envelope.getMessage().getId() );
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in markAsSent", e );
        }
    }


    public void incrementDeliveryAttempts( Envelope envelope )
    {
        try
        {
            dbUtil.update( "update messages set attempts = attempts + 1 where messageId = ?",
                    envelope.getMessage().getId() );
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in incrementDeliveryAttempts", e );
        }
    }


    public void saveEnvelope( Envelope envelope ) throws DaoException
    {
        Type type = new TypeToken<Envelope>()
        {

        }.getType();
        try
        {
            dbUtil.update( "merge into target_peers ( targetPeerId) values ( ? )", envelope.getTargetPeerId() );
            dbUtil.update(
                    "insert into messages ( messageId, targetPeerId, envelope, timeToLive) values ( ?, ?, ?, ? )",
                    envelope.getMessage().getId(), envelope.getTargetPeerId(),
                    new StringReader( JsonUtil.toJson( envelope, type ) ), envelope.getTimeToLive() );
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in saveEnvelope", e );
            throw new DaoException( e );
        }
    }


    public Envelope getEnvelope( UUID messageId ) throws DaoException
    {
        try
        {
            ResultSet resultSet =
                    dbUtil.select( "select envelope, isSent, createDate from messages where messageId = ?", messageId );
            Set<Envelope> envelopes = retrieveEnvelopsFromResultSet( resultSet );

            if ( !CollectionUtil.isCollectionEmpty( envelopes ) )
            {
                return envelopes.iterator().next();
            }
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in getEnvelope", e );
            throw new DaoException( e );
        }

        return null;
    }





    public Set<Envelope> retrieveEnvelopsFromResultSet( ResultSet resultSet )
    {
        Set<Envelope> envelopes = Sets.newHashSet();

        try
        {
            while ( resultSet != null && resultSet.next() )
            {
                Clob envelopeClob = resultSet.getClob( "envelope" );
                if ( envelopeClob != null && envelopeClob.length() > 0 )
                {
                    String envelopeJson = envelopeClob.getSubString( 1, ( int ) envelopeClob.length() );

                    Envelope envelope = JsonUtil.fromJson( envelopeJson, Envelope.class );
                    envelope.setSent( resultSet.getBoolean( "isSent" ) );
                    envelope.setCreateDate( resultSet.getTimestamp( "createDate" ) );

                    envelopes.add( envelope );
                }
            }
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in retrieveEnvelopsFromResultSet", e );
        }

        return envelopes;
    }
}
