package io.subutai.core.env.impl.dao;


import java.io.PrintStream;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.dao.DaoManager;
import io.subutai.core.env.impl.TestUtil;

import io.subutai.core.env.impl.entity.EnvironmentContainerImpl;

import com.google.common.collect.Lists;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentContainerDataServiceTest
{
    @Mock
    DaoManager daoManager;
    @Mock
    EntityManager entityManager;
    @Mock
    RuntimeException exception;
    @Mock
    TypedQuery<EnvironmentContainerImpl> typedQuery;
    @Mock
    Query query;
    @Mock
    EnvironmentContainerImpl environmentContainer;

    EnvironmentContainerDataService service;


    @Before
    public void setUp() throws Exception
    {
        service = new EnvironmentContainerDataService( daoManager );
        when( daoManager.getEntityManagerFromFactory() ).thenReturn( entityManager );
    }


    @Test
    public void testFind() throws Exception
    {
        service.find( TestUtil.CONTAINER_ID.toString() );

        verify( entityManager ).find( EnvironmentContainerImpl.class, TestUtil.CONTAINER_ID.toString() );


        doThrow( exception ).when( entityManager ).find( eq( EnvironmentContainerImpl.class ), anyString() );

        service.find( TestUtil.CONTAINER_ID.toString() );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testGetAll() throws Exception
    {
        when( entityManager.createQuery( anyString(), eq( EnvironmentContainerImpl.class ) ) ).thenReturn( typedQuery );

        when( typedQuery.getResultList() ).thenReturn( Lists.newArrayList( environmentContainer ) );

        service.getAll();

        verify( typedQuery ).getResultList();

        doThrow( exception ).when( typedQuery ).getResultList();

        service.getAll();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testPersist() throws Exception
    {
        service.persist( environmentContainer );

        verify( daoManager ).commitTransaction( entityManager );

        doThrow( exception ).when( entityManager ).persist( environmentContainer );

        service.persist( environmentContainer );

        verify( daoManager ).rollBackTransaction( entityManager );
    }


    @Test
    public void testRemove() throws Exception
    {
        when( entityManager.find( EnvironmentContainerImpl.class, TestUtil.CONTAINER_ID.toString() ) )
                .thenReturn( environmentContainer );

        service.remove( TestUtil.CONTAINER_ID.toString() );

        verify( entityManager ).remove( environmentContainer );

        doThrow( exception ).when( entityManager ).remove( environmentContainer );

        service.remove( TestUtil.CONTAINER_ID.toString() );

        verify( daoManager ).rollBackTransaction( entityManager );
    }


    @Test
    public void testUpdate() throws Exception
    {
        service.update( environmentContainer );

        verify( entityManager ).merge( environmentContainer );

        doThrow( exception ).when( entityManager ).merge( environmentContainer );

        service.update( environmentContainer );

        verify( daoManager ).rollBackTransaction( entityManager );
    }
}
