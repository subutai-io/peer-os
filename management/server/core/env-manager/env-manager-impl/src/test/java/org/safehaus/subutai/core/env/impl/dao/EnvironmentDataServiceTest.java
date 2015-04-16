package org.safehaus.subutai.core.env.impl.dao;


import java.io.PrintStream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.core.env.impl.TestUtil;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentDataServiceTest
{
    @Mock
    DaoManager daoManager;
    @Mock
    EntityManager entityManager;
    @Mock
    TypedQuery<EnvironmentImpl> typedQuery;
    @Mock
    EnvironmentImpl environment;
    @Mock
    RuntimeException exception;

    EnvironmentDataService service;


    @Before
    public void setUp() throws Exception
    {
        service = new EnvironmentDataService( daoManager );
        when( daoManager.getEntityManagerFromFactory() ).thenReturn( entityManager );
    }


    @Test
    public void testFind() throws Exception
    {
        service.find( TestUtil.ENV_ID.toString() );

        verify( entityManager ).find( EnvironmentImpl.class, TestUtil.ENV_ID.toString() );

        doThrow( exception ).when( entityManager ).find( EnvironmentImpl.class, TestUtil.ENV_ID.toString() );

        service.find( TestUtil.ENV_ID.toString() );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testGetAll() throws Exception
    {
        when( entityManager.createQuery( anyString(), eq( EnvironmentImpl.class ) ) ).thenReturn( typedQuery );

        service.getAll();

        verify( typedQuery ).getResultList();

        doThrow( exception ).when( typedQuery ).getResultList();

        service.getAll();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testPersist() throws Exception
    {
        service.persist( environment );

        verify( entityManager ).persist( environment );


        doThrow( exception ).when( entityManager ).persist( environment );

        service.persist( environment );

        verify( daoManager ).rollBackTransaction( entityManager );
    }


    @Test
    public void testRemove() throws Exception
    {
        when( entityManager.find( EnvironmentImpl.class, TestUtil.ENV_ID.toString() ) ).thenReturn( environment );

        service.remove( TestUtil.ENV_ID.toString() );

        verify( entityManager ).remove( environment );

        doThrow( exception ).when( entityManager ).remove( environment );

        service.remove( TestUtil.ENV_ID.toString() );

        verify( daoManager ).rollBackTransaction( entityManager );
    }


    @Test
    public void testUpdate() throws Exception
    {
        service.update( environment );

        verify( entityManager ).merge( environment );

        doThrow( exception ).when( entityManager ).merge( environment );

        service.update( environment );

        verify( daoManager ).rollBackTransaction( entityManager );
    }
}
