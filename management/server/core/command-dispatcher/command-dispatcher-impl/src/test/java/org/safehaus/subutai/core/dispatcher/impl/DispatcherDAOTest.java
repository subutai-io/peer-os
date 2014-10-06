package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import static junit.framework.TestCase.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for DispatcherDAO
 */
public class DispatcherDAOTest
{
    DbManager dbManager;
    DispatcherDAO dispatcherDAO;
    private static final UUID commandId = UUID.randomUUID();


    @Before
    public void setupMethod() throws DBException
    {
        dbManager = mock( DbManager.class );
        dispatcherDAO = new DispatcherDAO( dbManager );
    }


    private static class CustomIterator implements Iterator
    {
        Row row;
        AtomicBoolean atomicBoolean;


        public CustomIterator( String info )
        {
            atomicBoolean = new AtomicBoolean( true );
            row = mock( Row.class );
            when( row.getString( "info" ) ).thenReturn( info );
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


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new DispatcherDAO( null );
    }


    @Test
    public void testGetRemoteResponses() throws Exception
    {
        ResultSet rsQuery = mock( ResultSet.class );
        Response response = JsonUtil.fromJson( String.format( "{taskUuid:%s}", commandId ), Response.class );
        RemoteResponse remoteResponse = new RemoteResponse( response );
        CustomIterator itQuery = new CustomIterator( JsonUtil.toJson( remoteResponse ) );
        when( rsQuery.iterator() ).thenReturn( itQuery );
        when( dbManager.executeQuery2( anyString(), anyVararg() ) ).thenReturn( rsQuery );


        Set<RemoteResponse> remoteResponseSet = dispatcherDAO.getRemoteResponses( commandId );

        assertFalse( remoteResponseSet.isEmpty() );
    }


    @Test( expected = DBException.class )
    public void testGetRemoteResponses2() throws Exception
    {
        ResultSet rsQuery = mock( ResultSet.class );
        Response response = JsonUtil.fromJson( String.format( "{taskUuid:%s}", commandId ), Response.class );
        RemoteResponse remoteResponse = new RemoteResponse( response );
        CustomIterator itQuery = new CustomIterator( "invalid json here" );
        when( rsQuery.iterator() ).thenReturn( itQuery );
        when( dbManager.executeQuery2( anyString(), anyVararg() ) ).thenReturn( rsQuery );


        dispatcherDAO.getRemoteResponses( commandId );
    }
}
