package org.safehaus.subutai.core.registry.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class TemplateDAOTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateDAOTest.class.getName() );

    //    private EntityManager em;
    private EntityManagerFactory emf;
    //    private EntityTransaction tx;

    private TemplateDAO templateDAO;


    @Before
    public void setUp() throws Exception
    {
        emf = Persistence.createEntityManagerFactory( "default" );
        EntityManager em = emf.createEntityManager();
        templateDAO = new TemplateDAO();
        templateDAO.setEntityManagerFactory( emf );
    }


    @After
    public void tearDown() throws Exception
    {
        //        em.close();
        emf.close();
    }


    @Test
    public void testSaveTemplate() throws Exception
    {
        Template template = TestUtils.getParentTemplate();
        templateDAO.saveTemplate( template );
        Template savedTemplate = templateDAO.getTemplateByName( template.getTemplateName(), template.getLxcArch() );
        assertEquals( template, savedTemplate );
    }


    @Ignore
    @Test
    public void testGetAllTemplates() throws Exception
    {
        List<Template> templates = new ArrayList<>();
        templates.add( TestUtils.getParentTemplate() );
        templates.add( TestUtils.getChildTemplate() );

        LOGGER.info( "Templates going to be persisted" );
        for ( Template template : templates )
        {
            templateDAO.saveTemplate( template );
            LOGGER.warn( template.getTemplateName() );
        }

        LOGGER.info( "Templates persisted in database" );
        List<Template> savedTemplates = templateDAO.getAllTemplates();
        for ( Template savedTemplate : savedTemplates )
        {
            LOGGER.warn( savedTemplate.getTemplateName() );
        }

        assertArrayEquals( templates.toArray(), savedTemplates.toArray() );
    }


    @Ignore
    @Test
    public void testRemoveTemplate() throws Exception
    {
        LOGGER.warn( "\n\n\n\nTesting Remove Template" );
        Template template = TestUtils.getParentTemplate();
        templateDAO.saveTemplate( template );

        Template savedTemplate = templateDAO.getTemplateByName( template.getTemplateName(), template.getLxcArch() );
        LOGGER.warn( template.toString() );
        LOGGER.warn( savedTemplate.toString() );
        LOGGER.warn( "\n\n\n\nGetting all templates from DB" );
        for ( Template template1 : templateDAO.getAllTemplates() )
        {
            LOGGER.warn( template1.getTemplateName() );
        }
        assertEquals( template, savedTemplate );
        templateDAO.removeTemplate( template );
        savedTemplate = templateDAO.getTemplateByName( template.getTemplateName(), template.getLxcArch() );
        LOGGER.warn( "\n\n\n\nGetting template from database" );
        LOGGER.warn( savedTemplate.toString() );
        assertNotNull( savedTemplate );
    }


    @Ignore
    @Test
    public void testGetChildTemplates() throws Exception
    {
        Template parentTemplate = TestUtils.getParentTemplate();
        Template childTemplate = TestUtils.getChildTemplate();
        parentTemplate.addChildren( Arrays.asList( childTemplate ) );
        templateDAO.saveTemplate( parentTemplate );
        List<Template> childTemplates =
                templateDAO.getChildTemplates( parentTemplate.getTemplateName(), parentTemplate.getLxcArch() );
        assertTrue( childTemplates.contains( childTemplate ) );
    }


    //    @Ignore
    @Test
    public void testGetTemplateByName() throws Exception
    {
        Template template = TestUtils.getParentTemplate();
        templateDAO.saveTemplate( template );
        Template savedTemplate = templateDAO.getTemplateByName( template.getTemplateName(), template.getLxcArch() );
        assertNotEquals( template, savedTemplate );
    }
}