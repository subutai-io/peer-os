package org.safehaus.subutai.core.dispatcher.impl;


import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.JsonUtil;

import com.sun.rowset.internal.Row;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.mock;


/**
 * Test for DispatcherDAO
 */
@Ignore
public class DispatcherDAOTest
{
    DataSource dataSource;
    DispatcherDAO dispatcherDAO;
    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final UUID PEER_ID = UUID.randomUUID();
    private static final String INVALID_JSON = "invalid json here";
    private static final Response RESPONSE =
            JsonUtil.fromJson( String.format( "{taskUuid:%s}", COMMAND_ID ), Response.class );


    @Before
    public void setupMethod() throws DaoException
    {
        dataSource = mock( DataSource.class );
        dispatcherDAO = new DispatcherDAO( dataSource );
    }


    private static class CustomIterator implements Iterator
    {
        Row row;
        AtomicBoolean atomicBoolean;


        public CustomIterator( String info )
        {
            atomicBoolean = new AtomicBoolean( true );
            row = mock( Row.class );
            //            when( row.getString( "info" ) ).thenReturn( info );
        }


        @Override
        public boolean hasNext()
        {
            return atomicBoolean.getAndSet( false );
        }


        @Override
        public Object next()
        {
            return row;
        }


        @Override
        public void remove()
        {

        }
    }


    @Test(expected = NullPointerException.class)
    public void testConstructor() throws Exception
    {
        new DispatcherDAO( null );
    }


    @Test
    public void testGetRemoteResponses() throws Exception
    {
        ResultSet rsQuery = mock( ResultSet.class );
        RemoteResponse remoteResponse = new RemoteResponse( RESPONSE );
        CustomIterator itQuery = new CustomIterator( JsonUtil.toJson( remoteResponse ) );
        //        when( rsQuery.iterator() ).thenReturn( itQuery );
        //        when( dataSource.executeQuery2( anyString(), anyVararg() ) ).thenReturn( rsQuery );


        Set<RemoteResponse> remoteResponseSet = dispatcherDAO.getRemoteResponses( COMMAND_ID );

        assertFalse( remoteResponseSet.isEmpty() );
    }


    @Test(expected = DaoException.class)
    public void testGetRemoteResponses2() throws Exception
    {
        ResultSet rsQuery = mock( ResultSet.class );
        CustomIterator itQuery = new CustomIterator( INVALID_JSON );
        //        when( rsQuery.iterator() ).thenReturn( itQuery );
        //        when( dataSource.executeQuery2( anyString(), anyVararg() ) ).thenReturn( rsQuery );


        dispatcherDAO.getRemoteResponses( COMMAND_ID );
    }


    @Test
    public void testSaveRemoteResponse() throws Exception
    {
        RemoteResponse remoteResponse = new RemoteResponse( RESPONSE );


        dispatcherDAO.saveRemoteResponse( remoteResponse );


        //        verify( dataSource ).executeUpdate2( anyString(), anyVararg() );
    }


    @Test
    public void testDeleteRemoteResponse() throws Exception
    {
        RemoteResponse remoteResponse = new RemoteResponse( RESPONSE );

        dispatcherDAO.deleteRemoteResponse( remoteResponse );


        //        verify( dataSource ).executeUpdate2( anyString(), anyVararg() );
    }


    @Test
    public void testDeleteRemoteResponses() throws Exception
    {


        dispatcherDAO.deleteRemoteResponses( COMMAND_ID );


        //        verify( dataSource ).executeUpdate2( anyString(), anyVararg() );
    }


    @Test
    public void testSaveRemoteRequest() throws Exception
    {
        RemoteRequest remoteRequest = new RemoteRequest( PEER_ID, COMMAND_ID, 1 );


        dispatcherDAO.saveRemoteRequest( remoteRequest );


        //        verify( dataSource ).executeUpdate2( anyString(), anyVararg() );
    }


    @Test
    public void testDeleteRemoteRequest() throws Exception
    {

        dispatcherDAO.deleteRemoteRequest( COMMAND_ID );


        //        verify( dataSource ).executeUpdate2( anyString(), anyVararg() );
    }


    @Test
    public void testDeleteRemoteRequest2() throws Exception
    {

        dispatcherDAO.deleteRemoteRequestWithAttempts( COMMAND_ID, 1 );


        //        verify( dataSource ).executeUpdate2( anyString(), anyVararg() );
    }


    @Test
    public void testGetRemoteRequests() throws Exception
    {

        ResultSet rsQuery = mock( ResultSet.class );
        RemoteRequest remoteRequest = new RemoteRequest( PEER_ID, COMMAND_ID, 1 );
        CustomIterator itQuery = new CustomIterator( JsonUtil.toJson( remoteRequest ) );
        //        when( rsQuery.iterator() ).thenReturn( itQuery );
        //        when( dataSource.executeQuery2( anyString(), anyVararg() ) ).thenReturn( rsQuery );


        Set<RemoteRequest> remoteRequests = dispatcherDAO.getRemoteRequests( 1, 1 );


        assertFalse( remoteRequests.isEmpty() );
    }


    @Test(expected = DaoException.class)
    public void testGetRemoteRequests2() throws Exception
    {

        ResultSet rsQuery = mock( ResultSet.class );
        RemoteRequest remoteRequest = new RemoteRequest( PEER_ID, COMMAND_ID, 1 );
        CustomIterator itQuery = new CustomIterator( INVALID_JSON );
        //        when( rsQuery.iterator() ).thenReturn( itQuery );
        //        when( dataSource.executeQuery2( anyString(), anyVararg() ) ).thenReturn( rsQuery );


        Set<RemoteRequest> remoteRequests = dispatcherDAO.getRemoteRequests( 1, 1 );


        assertFalse( remoteRequests.isEmpty() );
    }


    @Test
    public void testGetRemoteRequest() throws Exception
    {

        ResultSet rsQuery = mock( ResultSet.class );
        RemoteRequest remoteRequest = new RemoteRequest( PEER_ID, COMMAND_ID, 1 );
        CustomIterator itQuery = new CustomIterator( JsonUtil.toJson( remoteRequest ) );
        //        when( rsQuery.iterator() ).thenReturn( itQuery );
        //        when( rsQuery.one() ).thenReturn( ( Row ) itQuery.next() );
        //        when( dataSource.executeQuery2( anyString(), anyVararg() ) ).thenReturn( rsQuery );


        RemoteRequest remoteRequest1 = dispatcherDAO.getRemoteRequest( COMMAND_ID );


        assertEquals( remoteRequest.getCommandId(), remoteRequest1.getCommandId() );
    }


    @Test(expected = DaoException.class)
    public void testGetRemoteRequest2() throws Exception
    {

        ResultSet rsQuery = mock( ResultSet.class );
        RemoteRequest remoteRequest = new RemoteRequest( PEER_ID, COMMAND_ID, 1 );
        CustomIterator itQuery = new CustomIterator( INVALID_JSON );
        //        when( rsQuery.iterator() ).thenReturn( itQuery );
        //        when( rsQuery.one() ).thenReturn( ( Row ) itQuery.next() );
        //        when( dataSource.executeQuery2( anyString(), anyVararg() ) ).thenReturn( rsQuery );


        RemoteRequest remoteRequest1 = dispatcherDAO.getRemoteRequest( COMMAND_ID );


        assertEquals( remoteRequest.getCommandId(), remoteRequest1.getCommandId() );
    }
}
