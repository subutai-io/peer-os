package org.safehaus.subutai.common.protocol.impl;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.service.TemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by talas on 10/29/14.
 */
public class TemplateServiceImpl implements TemplateService
{
    private EntityManagerFactory entityManagerFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateServiceImpl.class.getName() );


    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;
    }


    @Override
    public Template createTemplate( Template template )
    {
        if ( template.getTemplateName() == null || "".equals( template.getTemplateName() ) )
        {
            throw new RuntimeException( "Template Name is required" );
        }

        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.persist( template );
            entityManager.flush();
            entityManager.getTransaction().commit();
        }
        catch ( Exception ex )
        {
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
        return template;
    }


    @Override
    public Template getTemplate( final long id )
    {
        EntityManager entityManager = null;
        Template template = null;

        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            template = entityManager.find( Template.class, id );
            entityManager.getTransaction().commit();
        }
        catch ( EntityNotFoundException ex )
        {
            LOGGER.error( "Template not found with id: " + String.valueOf( id ), ex );
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Error in getTemplate method", ex );
        }
        finally
        {
            if ( entityManager != null )
            {
                if ( entityManager.getTransaction().isActive() )
                {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }
        return template;
    }


    @Override
    public List<Template> getTemplates()
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
        finally
        {
            if ( entityManager != null )
            {
                entityManager.close();
            }
        }
    }


    @Override
    public void deleteTemplate( final long id )
    {
        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            Template template = entityManager.find( Template.class, id );
            entityManager.remove( template );
            entityManager.getTransaction().commit();
            LOGGER.info( String.format( "Template deleted with id: %d", id ) );
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Exception deleting template with id: %d", id );
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
}
