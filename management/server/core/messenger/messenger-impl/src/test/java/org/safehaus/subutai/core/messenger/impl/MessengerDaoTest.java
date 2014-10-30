package org.safehaus.subutai.core.messenger.impl;


import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.DbUtil;


@RunWith( MockitoJUnitRunner.class )
public class MessengerDaoTest
{
    @Mock
    DbUtil dbUtil;
    @Mock
    DataSource dataSource;


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


    @Before
    public void setUp() throws Exception
    {
        messengerDao = new MessengerDaoExt( dataSource );
        messengerDao.setDbUtil( dbUtil );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new MessengerDaoExt( null );
    }
}
