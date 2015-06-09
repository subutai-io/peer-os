package org.safehaus.subutai.core.identity.impl.dao;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.identity.impl.entity.CliCommandEntity;
import org.safehaus.subutai.core.identity.impl.entity.CliCommandPK;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CliCommandDataServiceTest
{
    private CliCommandDataService cliCommandDataService;

    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction transaction;
    @Mock
    Query query;
    @Mock
    CliCommandPK cliCommandPK;
    @Mock
    CliCommandEntity cliCommandEntity;
    @Mock
    TypedQuery<Object> typedQuery;


    @Before
    public void setUp() throws Exception
    {
        cliCommandDataService = new CliCommandDataService( entityManagerFactory );
        cliCommandDataService.setEmf( entityManagerFactory );

        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        when( entityManager.getTransaction() ).thenReturn( transaction );
        when( entityManager.createQuery( anyString() ) ).thenReturn( query );
        when( entityManager.createQuery( anyString(), Matchers.<Class<Object>>anyObject() ) ).thenReturn( typedQuery );
    }


    @Test
    public void testGetAll() throws Exception
    {
        cliCommandDataService.getAll();

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testGetAllException() throws Exception
    {
        when( typedQuery.getResultList() ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );

        cliCommandDataService.getAll();

        verify( transaction ).rollback();
    }


    @Test
    public void testFind() throws Exception
    {
        when( cliCommandPK.getName() ).thenReturn( "name" );
        when( cliCommandPK.getScope() ).thenReturn( "scope" );

        cliCommandDataService.find( cliCommandPK );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testFindException() throws Exception
    {
        when( cliCommandPK.getName() ).thenReturn( "name" );
        when( cliCommandPK.getScope() ).thenReturn( "scope" );
        when( transaction.isActive() ).thenReturn( true );
        when( typedQuery.getResultList() ).thenThrow( Exception.class );

        cliCommandDataService.find( cliCommandPK );

        verify( transaction ).rollback();
    }


    @Test
    public void testPersist() throws Exception
    {
        cliCommandDataService.persist( cliCommandEntity );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test( expected = Exception.class )
    public void testPersistException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        cliCommandDataService.persist( cliCommandEntity );

        verify( transaction ).rollback();
    }


    @Test
    public void testRemove() throws Exception
    {
        cliCommandDataService.remove( cliCommandPK );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test
    public void testRemoveException() throws Exception
    {
        when( query.executeUpdate() ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );
        cliCommandDataService.remove( cliCommandPK );
    }


    @Test
    public void testUpdate() throws Exception
    {
        cliCommandDataService.update( cliCommandEntity );

        verify( transaction ).begin();
        verify( transaction ).commit();
        verify( entityManager ).close();
    }


    @Test( expected = Exception.class )
    public void testUpdateException() throws Exception
    {
        when( entityManager.getTransaction() ).thenThrow( Exception.class );

        cliCommandDataService.update( cliCommandEntity );

        verify( transaction ).rollback();
    }


    @Test
    public void testGetEmf() throws Exception
    {
        assertNotNull( cliCommandDataService.getEmf() );
    }
}