package io.subutai.core.environment.impl.dao;


import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentServiceImplTest
{
    private static final String ID = "q23";
    @Mock
    EntityManager entityManager;
    @Mock
    TypedQuery<LocalEnvironment> query;
    @Mock
    LocalEnvironment environment;
    @Mock
    EnvironmentContainerImpl environmentContainer;


    EnvironmentServiceImpl environmentService;


    @Before
    public void setUp() throws Exception
    {
        environmentService = new EnvironmentServiceImpl();
        environmentService.setEntityManager( entityManager );
        doReturn( query ).when( entityManager ).createQuery( anyString(), eq( LocalEnvironment.class ) );
    }


    @Test
    public void testFind() throws Exception
    {
        environmentService.find( ID );

        verify( entityManager ).find( LocalEnvironment.class, ID );
    }


    @Test
    public void testGetAll() throws Exception
    {
        environmentService.getAll();

        verify( entityManager ).createQuery( "select e from LocalEnvironment e where e.deleted = false", LocalEnvironment.class );

        verify( query ).getResultList();
    }


    @Test
    public void testPersist() throws Exception
    {
        environmentService.persist( environment );

        verify( entityManager ).persist( environment );
    }


    @Test
    public void testRemove() throws Exception
    {
        environmentService.remove( ID );

        verify( entityManager ).remove( Matchers.anyObject() );
    }


    @Test
    public void testMerge() throws Exception
    {

        environmentService.merge( environment );

        verify( entityManager ).merge( environment );
    }


    @Test
    public void testMergeContainer() throws Exception
    {
        environmentService.mergeContainer( environmentContainer );

        verify( entityManager ).merge( environmentContainer );
    }
}
