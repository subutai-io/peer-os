package org.safehaus.subutai.core.messenger.impl;


import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.DbUtil;
import org.slf4j.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class MessengerDaoTest
{
    @Mock
    DbUtil dbUtil;
    @Mock
    DataSource dataSource;
    @Mock
    Logger logger;


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


    MessengerDaoExt messengerDao;


    private void throwDbException() throws SQLException
    {
        doThrow( new SQLException() ).when( dbUtil ).update( anyString(), anyVararg() );
    }


    @Before
    public void setUp() throws Exception
    {
        messengerDao = new MessengerDaoExt( dataSource );
        messengerDao.setDbUtil( dbUtil );
        messengerDao.LOG = logger;
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

        verify( logger ).error( anyString(), isA( SQLException.class ) );
    }
}
