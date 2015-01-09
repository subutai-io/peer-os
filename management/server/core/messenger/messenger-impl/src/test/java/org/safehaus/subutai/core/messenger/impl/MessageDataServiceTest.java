package org.safehaus.subutai.core.messenger.impl;


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
import org.safehaus.subutai.core.messenger.impl.dao.MessageDataService;
import org.safehaus.subutai.core.messenger.impl.entity.MessageEntity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessageDataServiceTest
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

    MessageDataService messageDataService;


    @Before
    public void setUp() throws Exception
    {
        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        when( entityManager.getTransaction() ).thenReturn( transaction );
        when( transaction.isActive() ).thenReturn( true );
        when( entityManager.createQuery( anyString() ) ).thenReturn( query );
        when( entityManager.createQuery( anyString(), eq( String.class ) ) ).thenReturn( typedQuery );
        when( entityManager.createQuery( anyString(), eq( MessageEntity.class ) ) ).thenReturn( typedQuery );

        messageDataService = new MessageDataService( entityManagerFactory );

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
        messageDataService.find( ID );

        verify( entityManager ).find( MessageEntity.class, ID );


        throwException();

        messageDataService.find( ID );

        verifyException();
    }


    @Test
    public void testGetAll() throws Exception
    {
        messageDataService.getAll();

        verify( entityManager ).createQuery( anyString(), eq( MessageEntity.class ) );

        throwException();
        messageDataService.getAll();

        verifyException();
    }


    @Test
    public void testPersist() throws Exception
    {
        messageDataService.persist( messageEntity );

        verify( entityManager ).persist( messageEntity );


        throwException();

        messageDataService.persist( messageEntity );

        verifyException();
    }


    @Test
    public void testUpdate() throws Exception
    {
        messageDataService.update( messageEntity );

        verify( entityManager ).merge( messageEntity );


        throwException();

        messageDataService.update( messageEntity );

        verifyException();
    }


    @Test
    public void testRemove() throws Exception
    {
        messageDataService.remove( ID );

        verify( entityManager ).remove( anyObject() );


        throwException();

        messageDataService.remove( ID );

        verifyException();
    }


    @Test
    public void testGetTargetPeers() throws Exception
    {
        messageDataService.getTargetPeers();

        verify( entityManager ).createQuery( anyString(), eq( String.class ) );


        throwException();

        messageDataService.getTargetPeers();

        verifyException();
    }


    @Test
    public void testGetMessages() throws Exception
    {
        messageDataService.getMessages( ID, 1, 1 );

        verify( entityManager ).createQuery( anyString(), eq( MessageEntity.class ) );


        throwException();

        messageDataService.getMessages( ID, 1, 1 );

        verifyException();
    }


    @Test
    public void testPurgeMessages() throws Exception
    {
        messageDataService.purgeMessages();

        verify( entityManager ).createQuery( anyString() );


        throwException();

        messageDataService.purgeMessages();

        verifyException();
    }


    @Test
    public void testMarkAsSent() throws Exception
    {
        messageDataService.markAsSent( ID );

        verify( entityManager ).createQuery( anyString() );


        throwException();

        messageDataService.markAsSent( ID );

        verifyException();
    }


    @Test
    public void testIncrementDeliveryAttempts() throws Exception
    {
        messageDataService.incrementDeliveryAttempts( ID );

        verify( entityManager ).createQuery( anyString() );


        throwException();

        messageDataService.incrementDeliveryAttempts( ID );

        verifyException();
    }
}
