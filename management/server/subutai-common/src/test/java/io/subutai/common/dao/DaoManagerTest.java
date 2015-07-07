package io.subutai.common.dao;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.dao.DaoManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class DaoManagerTest
{
    private DaoManager daoManager;

    @Mock
    EntityManager entityManager;
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityTransaction transaction;
    @Mock
    Object object;


    @Before
    public void setUp() throws Exception
    {
        daoManager = new DaoManager();
        daoManager.setEntityManager( entityManager );
        daoManager.setEntityManagerFactory( entityManagerFactory );

        when( entityManager.getTransaction() ).thenReturn( transaction );
    }


    @Test
    public void testInit() throws Exception
    {
        daoManager.init();
    }


    @Test
    public void testDestroy() throws Exception
    {
        when( entityManagerFactory.isOpen() ).thenReturn( true );

        daoManager.destroy();
        verify( entityManagerFactory ).close();
    }


    @Test
    public void testGetEntityManager() throws Exception
    {
        daoManager.getEntityManager();
    }


    @Test
    public void testGetEntityManagerFromFactory() throws Exception
    {
        daoManager.getEntityManagerFromFactory();
    }


    @Test
    public void testGetEntityManagerFactory() throws Exception
    {
        daoManager.getEntityManagerFactory();
    }


    @Test
    public void testRollBackTransaction() throws Exception
    {
        when( transaction.isActive() ).thenReturn( true );

        assertEquals( 1, daoManager.rollBackTransaction( entityManager ) );
    }


    @Test
    public void testStartTransaction() throws Exception
    {
        assertEquals( 1, daoManager.startTransaction( entityManager ) );
    }


    @Test
    public void testCommitTransaction() throws Exception
    {
        assertEquals( 1, daoManager.commitTransaction( entityManager ) );
    }


    @Test
    public void testCloseEntityManager() throws Exception
    {
        assertEquals( 1, daoManager.closeEntityManager( entityManager ) );
    }


    @Test
    public void testMergeExt() throws Exception
    {
        assertEquals( 1, daoManager.mergeExt( entityManager, object ) );
    }


    @Test
    public void testMergeExt2() throws Exception
    {
        assertEquals( 0, daoManager.mergeExt( null, object ) );
    }


    @Test
    public void testMergeExtException()
    {
        when( entityManager.merge( any() ) ).thenThrow( Exception.class );

        assertEquals( 0, daoManager.mergeExt( entityManager, object ) );
    }


    @Test
    public void testPersistExt() throws Exception
    {
        assertEquals( 1, daoManager.persistExt( entityManager, object ) );
    }


    @Test
    public void testPersistExt2() throws Exception
    {
        assertEquals( 0, daoManager.persistExt( null, object ) );
    }


    @Test
    public void testRemoveExt() throws Exception
    {
        assertEquals( 1, daoManager.removeExt( entityManager, object ) );
    }


    @Test
    public void testRemoveExt2() throws Exception
    {
        assertEquals( 0, daoManager.removeExt( null, object ) );
    }
}