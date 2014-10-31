package org.safehaus.subutai.common.protocol.impl;


import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.TemplateService;
import org.safehaus.subutai.common.settings.Common;
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
    public Template createTemplate( Template Template )
    {
        if ( Template.getTemplateName() == null || "".equals( Template.getTemplateName() ) )
        {
            throw new RuntimeException( "Template Name is required" );
        }

        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.persist( Template );
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
        return Template;
    }


    @Override
    public Template getTemplate( final long id )
    {
        EntityManager entityManager = null;
        Template Template = null;

        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            Template = entityManager.find( Template.class, id );
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
        return Template;
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
            Template Template = entityManager.find( Template.class, id );
            entityManager.remove( Template );
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


    @Override
    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch )
    {
        EntityManager em = null;
        try
        {
            Template template = this.getTemplateByName( parentTemplateName, lxcArch );
            return template.getChildren();
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
            return ( Template ) query.getSingleResult();
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


    public void test()
    {
        Template template =
                new Template( Common.DEFAULT_LXC_ARCH, UUID.randomUUID().toString(), "configPath", "subutaiParent",
                        "gitBranch", "gitUUID", "packageManifest", "md5sum" );
        createTemplate( template );
        List<Template> templates = getTemplates();
        for ( Template template1 : templates )
        {
            LOGGER.warn( "Template name saved in database: " + template1.getTemplateName() );
        }
    }
}
