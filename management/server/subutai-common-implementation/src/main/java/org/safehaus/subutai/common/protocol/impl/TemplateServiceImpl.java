package org.safehaus.subutai.common.protocol.impl;


import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.TemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class TemplateServiceImpl implements TemplateService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateServiceImpl.class.getName() );


    private EntityManagerFactory entityManagerFactory;


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        Preconditions.checkNotNull( entityManagerFactory, "EntityManagerFactory cannot be null value" );
        this.entityManagerFactory = entityManagerFactory;
        LOGGER.info( "EntityManagerFactory is assigned" );
    }


    /**
     * Saves template to database
     *
     * @param template - template to save
     */
    @Override
    public Template saveTemplate( Template template ) throws DaoException
    {
        Template savedTemplate = null;

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
            throw new DaoException( ex );
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


    /**
     * Deletes template from database
     *
     * @param template - template to delete
     */
    @Override
    public void removeTemplate( Template template ) throws DaoException
    {
        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            Query query = entityManager.createNamedQuery( Template.QUERY_REMOVE_TEMPLATE_BY_NAME_ARCH );
            query.setParameter( "templateName", template.getTemplateName() );
            query.setParameter( "lxcArch", template.getLxcArch() );
            query.executeUpdate();
            entityManager.getTransaction().commit();
            LOGGER.info( String.format( "Template deleted : %s", template.getTemplateName() ) );
        }
        catch ( Exception ex )
        {

            LOGGER.error( "Exception deleting template : %s", template.getTemplateName() );
            if ( entityManager != null )
            {
                if ( entityManager.getTransaction().isActive() )
                {
                    entityManager.getTransaction().rollback();
                }
            }
            throw new DaoException( ex );
        }
        finally
        {
            if ( entityManager != null )
            {
                entityManager.close();
            }
        }
    }


    /**
     * Returns template by name
     *
     * @param templateName - template name
     * @param lxcArch -- lxc arch of template
     *
     * @return {@code Template}
     */
    @Override
    public Template getTemplateByName( String templateName, String lxcArch ) throws DaoException
    {
        EntityManager entityManager;
        try
        {
            Template template;
            entityManager = entityManagerFactory.createEntityManager();
            Query query = entityManager.createNamedQuery( Template.QUERY_GET_TEMPLATE_BY_NAME_ARCH );
            query.setParameter( "templateName", templateName );
            query.setParameter( "lxcArch", lxcArch );
            template = ( Template ) query.getSingleResult();

            return template;
        }
        catch ( NoResultException e )
        {
            return null;
        }
        catch ( Exception ex )
        {
            throw new DaoException( ex );
        }
    }


    /**
     * Returns all registered templates from database
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getAllTemplates() throws DaoException
    {
        EntityManager entityManager;

        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            return entityManager.createNamedQuery( Template.QUERY_GET_ALL, Template.class ).getResultList();
        }
        catch ( Exception ex )
        {
            throw new DaoException( ex );
        }
    }


    /**
     * Returns child templates of supplied parent
     *
     * @param parentTemplateName - name of parent template
     * @param lxcArch - lxc arch of template
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch ) throws DaoException
    {
        try
        {
            Template template = this.getTemplateByName( parentTemplateName, lxcArch );
            if ( template != null )
            {
                return template.getChildren();
            }
            else
            {
                return Collections.emptyList();
            }
        }
        catch ( Exception ex )
        {
            throw new DaoException( ex );
        }
    }
}
