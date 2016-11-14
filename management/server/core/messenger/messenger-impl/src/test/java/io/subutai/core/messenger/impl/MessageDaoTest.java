package io.subutai.core.messenger.impl;


import java.io.PrintStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.messenger.impl.dao.MessageDao;
import io.subutai.core.messenger.impl.entity.MessageEntity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessageDaoTest
{
    private static final String ID = "id";
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction transaction;
    @Mock
    TypedQuery typedQuery;
    @Mock
    Query query;
    @Mock
    RuntimeException exception;
    @Mock
    MessageEntity messageEntity;

    MessageDao messageDao;


    @Before
    public void setUp() throws Exception
    {
        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        when( entityManager.getTransaction() ).thenReturn( transaction );
        when( transaction.isActive() ).thenReturn( true );
        when( entityManager.createQuery( anyString() ) ).thenReturn( query );
        when( entityManager.createQuery( anyString(), eq( String.class ) ) ).thenReturn( typedQuery );
        when( entityManager.createQuery( anyString(), eq( MessageEntity.class ) ) ).thenReturn( typedQuery );

        messageDao = new MessageDao( entityManagerFactory );

        reset( exception );
    }


    private void throwException()
    {
        doThrow( exception ).when( transaction ).begin();
    }


    private void verifyException()
    {
        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testFind() throws Exception
    {
        messageDao.find( ID );

        verify( entityManager ).find( MessageEntity.class, ID );


        throwException();

        messageDao.find( ID );

        verifyException();
    }


    @Test
    public void testGetAll() throws Exception
    {
        messageDao.getAll();

        verify( entityManager ).createQuery( anyString(), eq( MessageEntity.class ) );

        throwException();
        messageDao.getAll();

        verifyException();
    }


    @Test
    public void testPersist() throws Exception
    {
        messageDao.persist( messageEntity );

        verify( entityManager ).persist( messageEntity );


        throwException();

        messageDao.persist( messageEntity );

        verifyException();
    }


    @Test
    public void testUpdate() throws Exception
    {
        messageDao.update( messageEntity );

        verify( entityManager ).merge( messageEntity );


        throwException();

        messageDao.update( messageEntity );

        verifyException();
    }


    @Test
    public void testRemove() throws Exception
    {
        messageDao.remove( ID );

        verify( entityManager ).remove( anyObject() );


        throwException();

        messageDao.remove( ID );

        verifyException();
    }


    @Test
    public void testGetTargetPeers() throws Exception
    {
        messageDao.getTargetPeers();

        verify( entityManager ).createQuery( anyString(), eq( String.class ) );


        throwException();

        messageDao.getTargetPeers();

        verifyException();
    }


    @Test
    public void testGetMessages() throws Exception
    {
        messageDao.getMessages( ID, 1, 1 );

        verify( entityManager ).createQuery( anyString(), eq( MessageEntity.class ) );


        throwException();

        messageDao.getMessages( ID, 1, 1 );

        verifyException();
    }


    @Test
    public void testPurgeMessages() throws Exception
    {
        messageDao.purgeMessages();

        verify( entityManager ).createQuery( anyString() );


        throwException();

        messageDao.purgeMessages();

        verifyException();
    }


    @Test
    public void testMarkAsSent() throws Exception
    {
        messageDao.markAsSent( ID );

        verify( entityManager ).createQuery( anyString() );


        throwException();

        messageDao.markAsSent( ID );

        verifyException();
    }


    @Test
    public void testIncrementDeliveryAttempts() throws Exception
    {
        messageDao.incrementDeliveryAttempts( ID );

        verify( entityManager ).createQuery( anyString() );


        throwException();

        messageDao.incrementDeliveryAttempts( ID );

        verifyException();
    }
}
