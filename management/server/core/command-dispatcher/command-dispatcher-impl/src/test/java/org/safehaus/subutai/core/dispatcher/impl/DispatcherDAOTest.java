package org.safehaus.subutai.core.dispatcher.impl;


import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.JsonUtil;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for DispatcherDAO
 */
@RunWith( MockitoJUnitRunner.class )
public class DispatcherDAOTest
{

    @Mock
    DataSource dataSource;
    @Mock
    Connection connection;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    ResultSet resultSet;

    DispatcherDAO dispatcherDAO;
    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final UUID PEER_ID = UUID.randomUUID();
    private static final String INVALID_JSON = "invalid json here";
    private static final Response RESPONSE =
            JsonUtil.fromJson( String.format( "{taskUuid:%s}", COMMAND_ID ), Response.class );
    private static final RemoteRequest remoteRequest = new RemoteRequest( PEER_ID, COMMAND_ID, 1 );
    private static final RemoteResponse remoteResponse = new RemoteResponse( RESPONSE );


    @Before
    public void setupMethod() throws DaoException, SQLException
    {
        when( connection.prepareStatement( anyString() ) ).thenReturn( preparedStatement );
        when( dataSource.getConnection() ).thenReturn( connection );
        when( preparedStatement.executeQuery() ).thenReturn( resultSet );
        ResultSetMetaData metadata = mock( ResultSetMetaData.class );
        when( metadata.getColumnCount() ).thenReturn( 1 );
        when( metadata.getColumnName( 1 ) ).thenReturn( "info" );
        when( metadata.getColumnType( 1 ) ).thenReturn( java.sql.Types.CLOB );
        when( resultSet.getMetaData() ).thenReturn( metadata );
        when( resultSet.next() ).thenReturn( true ).thenReturn( false );

        dispatcherDAO = new DispatcherDAO( dataSource );
    }


    private void returnInvalidJson() throws SQLException, UnsupportedEncodingException
    {
        when( resultSet.getObject( 1 ) ).thenReturn( INVALID_JSON );
    }


    private void returnRequest() throws SQLException
    {
        when( resultSet.getObject( 1 ) ).thenReturn( JsonUtil.toJson( remoteRequest ) );
    }


    private void returnResponse() throws SQLException
    {
        when( resultSet.getObject( 1 ) ).thenReturn( JsonUtil.toJson( remoteResponse ) );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new DispatcherDAO( null );
    }


    @Test
    public void testGetRemoteResponses() throws Exception
    {

        returnResponse();

        Set<RemoteResponse> remoteResponseSet = dispatcherDAO.getRemoteResponses( COMMAND_ID );

        assertFalse( remoteResponseSet.isEmpty() );
    }


    @Test( expected = DaoException.class )
    public void testGetRemoteResponses2() throws Exception
    {
        returnInvalidJson();


        dispatcherDAO.getRemoteResponses( COMMAND_ID );
    }


    @Test
    public void testSaveRemoteResponse() throws Exception
    {
        RemoteResponse remoteResponse = new RemoteResponse( RESPONSE );


        dispatcherDAO.saveRemoteResponse( remoteResponse );


        verify( preparedStatement, times( 2 ) ).executeUpdate();
    }


    @Test
    public void testDeleteRemoteResponse() throws Exception
    {
        RemoteResponse remoteResponse = new RemoteResponse( RESPONSE );

        dispatcherDAO.deleteRemoteResponse( remoteResponse );


        verify( preparedStatement, times( 2 ) ).executeUpdate();
    }


    @Test
    public void testDeleteRemoteResponses() throws Exception
    {


        dispatcherDAO.deleteRemoteResponses( COMMAND_ID );


        verify( preparedStatement, times( 2 ) ).executeUpdate();
    }


    @Test
    public void testSaveRemoteRequest() throws Exception
    {
        RemoteRequest remoteRequest = new RemoteRequest( PEER_ID, COMMAND_ID, 1 );


        dispatcherDAO.saveRemoteRequest( remoteRequest );


        verify( preparedStatement, times( 2 ) ).executeUpdate();
    }


    @Test
    public void testDeleteRemoteRequest() throws Exception
    {

        dispatcherDAO.deleteRemoteRequest( COMMAND_ID );


        verify( preparedStatement, times( 2 ) ).executeUpdate();
    }


    @Test
    public void testGetRemoteRequests() throws Exception
    {

        returnRequest();


        Set<RemoteRequest> remoteRequests = dispatcherDAO.getRemoteRequests( 1, 1 );


        assertFalse( remoteRequests.isEmpty() );
    }


    @Test( expected = DaoException.class )
    public void testGetRemoteRequests2() throws Exception
    {

        returnInvalidJson();


        Set<RemoteRequest> remoteRequests = dispatcherDAO.getRemoteRequests( 1, 1 );


        assertFalse( remoteRequests.isEmpty() );
    }


    @Test
    public void testGetRemoteRequest() throws Exception
    {

        returnRequest();

        RemoteRequest remoteRequest1 = dispatcherDAO.getRemoteRequest( COMMAND_ID );


        assertEquals( remoteRequest.getCommandId(), remoteRequest1.getCommandId() );
    }


    @Test( expected = DaoException.class )
    public void testGetRemoteRequest2() throws Exception
    {
        returnInvalidJson();


        RemoteRequest remoteRequest1 = dispatcherDAO.getRemoteRequest( COMMAND_ID );


        assertEquals( remoteRequest.getCommandId(), remoteRequest1.getCommandId() );
    }
}
