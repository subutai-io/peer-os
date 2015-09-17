package io.subutai.common.protocol.impl;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.exception.DaoException;
import io.subutai.common.protocol.Template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TemplateServiceImplTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateServiceImplTest.class.getName() );

    private EntityManagerFactory emf;

    private TemplateServiceImpl templateServiceImpl;

    private EntityManagerFactory mockedEmf;
    private EntityManager mockedEm;
    private EntityTransaction transaction;

    private DaoManager daoManager;


    @Before
    public void setUp() throws Exception
    {
        emf = Persistence.createEntityManagerFactory( "default" );
        daoManager = new DaoManager();
        //daoManager.setEntityManagerFactory( emf );

        templateServiceImpl = new TemplateServiceImpl();
        templateServiceImpl.setDaoManager( daoManager );

        mockedEmf = mock( EntityManagerFactory.class );
        mockedEm = mock( EntityManager.class );
        transaction = mock( EntityTransaction.class );
        when( mockedEmf.createEntityManager() ).thenReturn( mockedEm );
        when( mockedEm.getTransaction() ).thenReturn( transaction, transaction, transaction );
        when( mockedEm.createQuery( anyString() ) ).thenThrow( Exception.class );
        when( mockedEm.merge( anyObject() ) ).thenThrow( Exception.class );
        when( transaction.isActive() ).thenReturn( true );
    }


    @After
    public void tearDown() throws Exception
    {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery( "DELETE FROM Template t" ).executeUpdate();
        em.getTransaction().commit();
    }



    @Test( expected = DaoException.class )
    public void testSaveTemplate() throws Exception
    {
        templateServiceImpl.saveTemplate( TestUtils.getParentTemplate() );
        assertTrue( templateServiceImpl.getAllTemplates().contains( TestUtils.getParentTemplate() ) );


        templateServiceImpl.saveTemplate( TestUtils.getParentTemplate() );
    }


    @Test( expected = DaoException.class )
    public void testRemoveTemplate() throws Exception
    {
        templateServiceImpl.saveTemplate( TestUtils.getParentTemplate() );
        assertTrue( templateServiceImpl.getAllTemplates().contains( TestUtils.getParentTemplate() ) );
        templateServiceImpl.removeTemplate( TestUtils.getParentTemplate() );
        assertFalse( templateServiceImpl.getAllTemplates().contains( TestUtils.getParentTemplate() ) );

        //templateServiceImpl.setEntityManagerFactory( mockedEmf );
        templateServiceImpl.removeTemplate( TestUtils.getParentTemplate() );
    }


    @Test( expected = DaoException.class )
    public void testGetTemplate() throws Exception
    {
        Template parentTemplate = TestUtils.getParentTemplate();
        templateServiceImpl.saveTemplate( parentTemplate );
        assertTrue( templateServiceImpl.getAllTemplates().contains( parentTemplate ) );
        assertNotNull(
                templateServiceImpl.getTemplate( parentTemplate.getTemplateName(), parentTemplate.getLxcArch() ) );

        //templateServiceImpl.setEntityManagerFactory( mockedEmf );
        templateServiceImpl.getTemplate( parentTemplate.getTemplateName(), parentTemplate.getLxcArch() );
    }


    @Test( expected = DaoException.class )
    public void testGetTemplate1() throws Exception
    {
        Template parentTemplate = TestUtils.getParentTemplate();
        templateServiceImpl.saveTemplate( parentTemplate );
        assertTrue( templateServiceImpl.getAllTemplates().contains( parentTemplate ) );
        assertNotNull( templateServiceImpl
                .getTemplate( parentTemplate.getTemplateName(), parentTemplate.getLxcArch(), parentTemplate.getMd5sum(),
                        parentTemplate.getTemplateVersion() ) );

        //templateServiceImpl.setEntityManagerFactory( mockedEmf );
        templateServiceImpl
                .getTemplate( parentTemplate.getTemplateName(), parentTemplate.getLxcArch(), parentTemplate.getMd5sum(),
                        parentTemplate.getTemplateVersion() );
    }


    @Test( expected = DaoException.class )
    public void testGetTemplate2() throws Exception
    {
        Template parentTemplate = TestUtils.getParentTemplate();

        templateServiceImpl.saveTemplate( parentTemplate );
        assertTrue( templateServiceImpl.getAllTemplates().contains( parentTemplate ) );
        assertNotNull( templateServiceImpl
                .getTemplate( parentTemplate.getTemplateName(), parentTemplate.getTemplateVersion(),
                        parentTemplate.getLxcArch() ) );

        //templateServiceImpl.setEntityManagerFactory( mockedEmf );
        templateServiceImpl.getTemplate( parentTemplate.getTemplateName(), parentTemplate.getTemplateVersion(),
                parentTemplate.getLxcArch() );
    }


    @Test( expected = DaoException.class )
    public void testGetAllTemplates() throws Exception
    {
        templateServiceImpl.saveTemplate( TestUtils.getParentTemplate() );
        templateServiceImpl.saveTemplate( TestUtils.getChildTemplate() );
        assertTrue( templateServiceImpl.getAllTemplates().contains( TestUtils.getParentTemplate() ) );
        assertTrue( templateServiceImpl.getAllTemplates().contains( TestUtils.getChildTemplate() ) );

        //templateServiceImpl.setEntityManagerFactory( mockedEmf );
        templateServiceImpl.getAllTemplates();
    }


    @Test( expected = DaoException.class )
    public void testGetChildTemplates() throws Exception
    {
        Template parentTemplate = TestUtils.getParentTemplate();
        Template childTemplate = TestUtils.getChildTemplate();
        templateServiceImpl.saveTemplate( parentTemplate );
        templateServiceImpl.saveTemplate( childTemplate );

        assertTrue(
                templateServiceImpl.getChildTemplates( parentTemplate.getTemplateName(), parentTemplate.getLxcArch() )
                                   .contains( childTemplate ) );

        //templateServiceImpl.setEntityManagerFactory( mockedEmf );
        templateServiceImpl.getChildTemplates( parentTemplate.getTemplateName(), parentTemplate.getLxcArch() );
    }


    @Test( expected = DaoException.class )
    public void testGetChildTemplates1() throws Exception
    {
        Template parentTemplate = TestUtils.getParentTemplate();
        Template childTemplate = TestUtils.getChildTemplate();
        templateServiceImpl.saveTemplate( parentTemplate );
        templateServiceImpl.saveTemplate( childTemplate );

        assertTrue( templateServiceImpl
                .getChildTemplates( parentTemplate.getTemplateName(), parentTemplate.getTemplateVersion(),
                        parentTemplate.getLxcArch() ).contains( childTemplate ) );

        //templateServiceImpl.setEntityManagerFactory( mockedEmf );
        templateServiceImpl.getChildTemplates( parentTemplate.getTemplateName(), parentTemplate.getTemplateVersion(),
                parentTemplate.getLxcArch() );
    }
}