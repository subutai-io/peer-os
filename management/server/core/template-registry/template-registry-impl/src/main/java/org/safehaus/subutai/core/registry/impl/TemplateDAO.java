package org.safehaus.subutai.core.registry.impl;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.TemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Provides Data Access API for templates
 */
public class TemplateDAO implements TemplateService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateDAO.class.getName() );
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    private EntityManagerFactory entityManagerFactory;


    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;
        LOGGER.info( "EntityManagerFactory is assigned" );
    }


    @Override
    public Template saveTemplate( Template template )
    {
        Template savedTemplate = null;
        if ( template.getTemplateName() == null || "".equals( template.getTemplateName() ) )
        {
            throw new RuntimeException( "Template Name is required" );
        }

        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            savedTemplate = entityManager.merge( template );
            entityManager.flush();
            entityManager.getTransaction().commit();
        }
        catch ( Exception ex )
        {
            LOGGER.warn( "Exception thrown in saveTemplate: ", ex );
            if ( entityManager != null )
            {
                if ( entityManager.getTransaction().isActive() )
                {
                    entityManager.getTransaction().rollback();
                }
            }
        }
        finally
        {
            if ( entityManager != null )
            {
                entityManager.close();
            }
        }
        return savedTemplate;
    }


    @Override
    public List<Template> getAllTemplates()
    {
        EntityManager entityManager = null;

        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            return entityManager.createNamedQuery( Template.QUERY_GET_ALL, Template.class ).getResultList();
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( ex );
        }
        //        finally
        //        {
        //            if ( entityManager != null )
        //            {
        //                entityManager.close();
        //            }
        //        }
    }


    @Override
    public void removeTemplate( Template template )
    {
        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            Query query = entityManager.createNamedQuery( Template.QUERY_REMOVE_TEMPLATE_BY_NAME_ARCH );
            query.setParameter( "templateName", template.getTemplateName() );
            query.setParameter( "lxcArch", template.getLxcArch() );
            entityManager.flush();
            entityManager.getTransaction().commit();
            LOGGER.info( String.format( "Template deleted : %s", template.getTemplateName() ) );
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Exception deleting template : %s", template.getTemplateName() );
            throw new RuntimeException( ex );
        }
        finally
        {
            if ( entityManager != null )
            {
                entityManager.close();
            }
        }
    }


    @Override
    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch )
    {
        try
        {
            Template template = this.getTemplateByName( parentTemplateName, lxcArch );
            return template.getChildren();
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( ex );
        }
    }


    @Override
    public Template getTemplateByName( String templateName, String lxcArch )
    {
        EntityManager em = null;
        try
        {
            em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            Query query = em.createNamedQuery( Template.QUERY_GET_TEMPLATE_BY_NAME_ARCH );
            query.setParameter( "templateName", templateName );
            query.setParameter( "lxcArch", lxcArch );
            em.getTransaction().commit();
            return ( Template ) query.getSingleResult();
        }
        catch ( org.apache.openjpa.persistence.PersistenceException ex )
        {
            LOGGER.warn( "Couldn't find template with name: " + templateName + ", lxcArch: " + lxcArch );
            return null;
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( ex );
        }
        finally
        {
            if ( em != null )
            {
                em.close();
            }
        }
    }
}
