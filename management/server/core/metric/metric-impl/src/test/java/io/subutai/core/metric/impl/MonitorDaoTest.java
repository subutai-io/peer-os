package io.subutai.core.metric.impl;


import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.exception.DaoException;
import io.subutai.core.metric.impl.model.Subscriber;
import io.subutai.core.metric.impl.model.SubscriberPK;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for MonitorDao
 */
@RunWith( MockitoJUnitRunner.class )
public class MonitorDaoTest
{

    private final static String SUBSCRIBER_ID = "subscriber";
    private final static String ENVIRONMENT_ID = UUID.randomUUID().toString();

    MonitorDaoExt monitorDao;

    private EntityManagerFactory emf;


    static class MonitorDaoExt extends MonitorDao
    {


        public MonitorDaoExt( final EntityManagerFactory emf ) throws DaoException
        {
            super( emf );
        }
    }


    private void throwDbException() throws SQLException
    {
        EntityManagerFactory emf = mock( EntityManagerFactory.class );
        EntityManager em = mock( EntityManager.class );
        EntityManager em1 = mock( EntityManager.class );
        EntityTransaction transaction = mock( EntityTransaction.class );
        when( transaction.isActive() ).thenReturn( false );
        when( em.getTransaction() ).thenThrow( new PersistenceException() ).thenReturn( transaction );
        when( emf.createEntityManager() ).thenReturn( em1 ).thenReturn( em );
        try
        {
            monitorDao = new MonitorDaoExt( emf );
        }
        catch ( DaoException e )
        {
            e.printStackTrace();
        }
    }


    @Before
    public void setUp() throws Exception
    {
        emf = Persistence.createEntityManagerFactory( "default" );

        monitorDao = new MonitorDaoExt( emf );
    }


    @Test
    public void testAddSubscription() throws Exception
    {
        monitorDao.addSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );

        assertTrue( monitorDao.getEnvironmentSubscribersIds( ENVIRONMENT_ID ).contains( SUBSCRIBER_ID ) );
    }


    @Test( expected = DaoException.class )
    public void testAddSubscriptionException() throws Exception
    {
        throwDbException();

        monitorDao.addSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );
    }


    @Test
    public void testRemoveSubscription() throws Exception
    {

        monitorDao.removeSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );

        assertFalse( monitorDao.getEnvironmentSubscribersIds( ENVIRONMENT_ID ).contains( SUBSCRIBER_ID ) );
    }


    @Test( expected = DaoException.class )
    public void testRemoveSubscriptionException() throws Exception
    {
        throwDbException();

        monitorDao.removeSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );
    }


    @Test
    public void testGetEnvironmentSubscribersIds() throws Exception
    {
        Set<String> subscribersIds = monitorDao.getEnvironmentSubscribersIds( ENVIRONMENT_ID );

        assertTrue( subscribersIds.contains( SUBSCRIBER_ID ) );
    }


    @Test( expected = DaoException.class )
    public void testGetEnvironmentSubscribersIdsException() throws Exception
    {
        throwDbException();

        monitorDao.getEnvironmentSubscribersIds( ENVIRONMENT_ID );
    }


    @Test
    public void testSubscriber() throws Exception
    {
        Subscriber subscriber = new Subscriber( ENVIRONMENT_ID.toString(), SUBSCRIBER_ID );

        assertEquals( ENVIRONMENT_ID, subscriber.getEnvironmentId() );
        assertEquals( SUBSCRIBER_ID, subscriber.getSubscriberId() );
    }


    @Test
    public void testSubscriberPK() throws Exception
    {
        SubscriberPK subscriberPK = new SubscriberPK();
        subscriberPK.setSubscriberId( SUBSCRIBER_ID );
        subscriberPK.setEnvironmentId( ENVIRONMENT_ID.toString() );

        assertEquals( ENVIRONMENT_ID, subscriberPK.getEnvironmentId() );
        assertEquals( SUBSCRIBER_ID, subscriberPK.getSubscriberId() );

        SubscriberPK subscriberPK2 = new SubscriberPK();
        subscriberPK2.setSubscriberId( SUBSCRIBER_ID );
        subscriberPK2.setEnvironmentId( ENVIRONMENT_ID.toString() );

        assertEquals( subscriberPK2, subscriberPK );
    }
}
