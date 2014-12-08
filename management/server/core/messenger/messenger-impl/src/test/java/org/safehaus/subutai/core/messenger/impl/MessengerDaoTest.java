package org.safehaus.subutai.core.messenger.impl;


import java.io.PrintStream;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.common.util.JsonUtil;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Ignore
@RunWith( MockitoJUnitRunner.class )
public class MessengerDaoTest
{

    private static final UUID TARGET_PEER_ID = UUID.randomUUID();
    private static final String RECIPIENT = "recipient";
    private static final int TIME_TO_LIVE = 5;
    private static final Timestamp CREATE_DATE = new Timestamp( System.currentTimeMillis() );

    private static final UUID SOURCE_PEER_ID = UUID.randomUUID();
    private static final Object PAYLOAD = new Object();

    @Mock
    DbUtil dbUtil;
    @Mock
    DataSource dataSource;
    @Mock
    SQLException exception;
    @Mock
    ResultSet envelopesRs;

    MessageImpl message;
    Envelope envelope;
    MessengerDaoExt messengerDao;


    static class MessengerDaoExt extends MessengerDao
    {
        public MessengerDaoExt( final DataSource dataSource ) throws DaoException
        {
            super( dataSource );
        }


        public void setDbUtil( DbUtil dbUtil )
        {
            this.dbUtil = dbUtil;
        }


        @Override
        public void setupDb()
        {
            //deactivate by overriding
        }


        public void testSetupDb() throws DaoException
        {
            super.setupDb();
        }
    }


    private void throwDbException() throws SQLException
    {
        doThrow( exception ).when( dbUtil ).update( anyString(), anyVararg() );
        doThrow( exception ).when( dbUtil ).select( anyString(), anyVararg() );
    }


    @Before
    public void setUp() throws Exception
    {
        messengerDao = new MessengerDaoExt( dataSource );
        messengerDao.setDbUtil( dbUtil );
        message = new MessageImpl( SOURCE_PEER_ID, PAYLOAD );
        envelope = new Envelope( message, TARGET_PEER_ID, RECIPIENT, TIME_TO_LIVE );
        envelope.setCreateDate( CREATE_DATE );
        when( envelopesRs.next() ).thenReturn( true ).thenReturn( false );
        Clob clob = mock( Clob.class );
        when( clob.length() ).thenReturn( 1L );
        when( clob.getSubString( anyLong(), anyInt() ) ).thenReturn( JsonUtil.toJson( envelope ) );
        when( envelopesRs.getClob( anyString() ) ).thenReturn( clob );
        when( envelopesRs.getBoolean( anyString() ) ).thenReturn( true );
        when( envelopesRs.getTimestamp( anyString() ) ).thenReturn( CREATE_DATE );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new MessengerDaoExt( null );
    }


    @Test
    public void testSetupDb() throws Exception
    {
        messengerDao.testSetupDb();

        verify( dbUtil ).update( anyString(), anyVararg() );
    }


    @Test( expected = DaoException.class )
    public void testSetupDbWithException() throws Exception
    {
        throwDbException();

        messengerDao.testSetupDb();
    }


    @Test
    public void testPurgeExpiredMessages() throws Exception
    {
        messengerDao.purgeExpiredMessages();

        verify( dbUtil, times( 2 ) ).update( anyString() );
    }


    @Test
    public void testPurgeExpiredMessagesWithException() throws Exception
    {
        throwDbException();

        messengerDao.purgeExpiredMessages();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testGetEnvelopes() throws Exception
    {
        ResultSet targetPeersRs = mock( ResultSet.class, "target_peers" );
        when( dbUtil.select( anyString(), anyVararg() ) ).thenReturn( targetPeersRs ).thenReturn( envelopesRs );
        when( targetPeersRs.next() ).thenReturn( true ).thenReturn( false );

        Set<Envelope> envelopes = messengerDao.getEnvelopes();

        Envelope envelope1 = envelopes.iterator().next();


        assertFalse( envelopes.isEmpty() );
        assertEquals( envelope.getRecipient(), envelope1.getRecipient() );
        assertEquals( envelope.getTargetPeerId(), envelope1.getTargetPeerId() );
        assertEquals( envelope.getCreateDate(), envelope1.getCreateDate() );

        throwDbException();

        messengerDao.getEnvelopes();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testMarkAsSent() throws Exception
    {
        messengerDao.markAsSent( envelope );

        verify( dbUtil ).update( anyString(), eq( envelope.getMessage().getId() ) );

        throwDbException();

        messengerDao.markAsSent( envelope );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testIncrementDeliveryAttempts() throws Exception
    {

        messengerDao.incrementDeliveryAttempts( envelope );

        verify( dbUtil ).update( anyString(), eq( envelope.getMessage().getId() ) );

        throwDbException();

        messengerDao.incrementDeliveryAttempts( envelope );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testSaveEnvelope() throws Exception
    {
        messengerDao.saveEnvelope( envelope );

        verify( dbUtil, times( 2 ) ).update( anyString(), anyVararg() );

        throwDbException();

        try
        {
            messengerDao.saveEnvelope( envelope );
            verify( exception ).printStackTrace( any( PrintStream.class ) );

            fail( "Expected DaoException" );
        }
        catch ( DaoException e )
        {

        }
    }


    @Test
    public void testGetEnvelope() throws Exception
    {
        when( dbUtil.select( anyString(), anyVararg() ) ).thenReturn( envelopesRs );

        Envelope envelope1 = messengerDao.getEnvelope( message.getId() );

        assertEquals( envelope.getRecipient(), envelope1.getRecipient() );
        assertEquals( envelope.getTargetPeerId(), envelope1.getTargetPeerId() );
        assertEquals( envelope.getCreateDate(), envelope1.getCreateDate() );

        throwDbException();

        try
        {
            messengerDao.getEnvelope( message.getId() );
            verify( exception ).printStackTrace( any( PrintStream.class ) );

            fail( "Expected DaoException" );
        }
        catch ( DaoException e )
        {

        }
    }
}
