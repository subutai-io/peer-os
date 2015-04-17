package org.safehaus.subutai.core.peer.impl.dao;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.peer.impl.entity.ContainerHostEntity;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostDataServiceTest
{
    private static final String ID = "id";
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager em;
    @Mock
    EntityTransaction transaction;
    @Mock
    RuntimeException exception;
    @Mock
    TypedQuery<ContainerHostEntity> typedQuery;
    @Mock
    ContainerHostEntity item;

    ContainerHostDataService service;


    @Before
    public void setUp() throws Exception
    {
        service = new ContainerHostDataService( entityManagerFactory );
        when( entityManagerFactory.createEntityManager() ).thenReturn( em );
        when( em.getTransaction() ).thenReturn( transaction );
        when( transaction.isActive() ).thenReturn( true );
        when( em.createQuery( anyString(), eq( ContainerHostEntity.class ) ) ).thenReturn( typedQuery );
    }

    private void verifyCommit()
    {
        verify( transaction ).commit();
    }


    private void verifyRollback()
    {
        verify( transaction ).rollback();
    }


    private void throwException()
    {
        doThrow( exception ).when( transaction ).begin();
    }


    @Test
    public void testFind() throws Exception
    {
        service.find( ID );

        verifyCommit();

        throwException();

        service.find( ID );

        verifyRollback();
    }


    @Test
    public void testGetAll() throws Exception
    {
        service.getAll();

        verifyCommit();

        throwException();

        service.getAll();

        verifyRollback();
    }


    @Test
    public void testPersist() throws Exception
    {

        service.persist( item );

        verifyCommit();

        throwException();

        service.persist( item );

        verifyRollback();
    }


    @Test
    public void testRemove() throws Exception
    {
        service.remove( ID );

        verifyCommit();

        throwException();

        service.remove( ID );

        verifyRollback();
    }


    @Test
    public void testUpdate() throws Exception
    {
        service.update( item );

        verifyCommit();

        throwException();

        service.update( item );

        verifyRollback();
    }

}
