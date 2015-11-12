package io.subutai.core.registry.impl.dao;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.datatypes.TemplateVersion;
import io.subutai.common.exception.DaoException;
import io.subutai.common.protocol.Template;


public class TemplateDataService
{
    private static final Logger LOG = LoggerFactory.getLogger( TemplateDataService.class.getName() );


    EntityManagerFactory emf;


    public TemplateDataService( EntityManagerFactory entityManagerFactory )
    {
        this.emf = entityManagerFactory;
    }


    /**
     * Saves template to database
     *
     * @param template - template to save
     */

    public Template saveTemplate( Template template ) throws DaoException
    {
        Template savedTemplate = null;

        EntityManager em = emf.createEntityManager();
        try
        {
            if ( template.getParentTemplateName() != null && !template.getParentTemplateName()
                                                                      .equals( template.getTemplateName() ) )
            {
                Template parent = getTemplate( template.getParentTemplateName(), template.getLxcArch() );
                if ( parent != null )
                {
                    em.getTransaction().begin();
                    savedTemplate = em.merge( template );
                    em.flush();
                    em.getTransaction().commit();
                    parent.addChildren( Arrays.asList( template ) );
                    saveTemplate( parent );
                }
                else
                {
                    throw new DaoException( "Parent template is null: " + template.getParentTemplateName() );
                }
            }
            else
            {
                em.getTransaction().begin();
                savedTemplate = em.merge( template );
                em.flush();
                em.getTransaction().commit();
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
            throw new DaoException( e );
        }
        finally
        {
            em.close();
        }
        return savedTemplate;
    }


    /**
     * Deletes template from database
     *
     * @param template - template to delete
     */
    public void removeTemplate( Template template ) throws DaoException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();

            Query query = em.createNamedQuery( Template.QUERY_REMOVE_TEMPLATE_BY_NAME_ARCH );
            query.setParameter( "templateName", template.getTemplateName() );
            query.setParameter( "lxcArch", template.getLxcArch() );
            query.executeUpdate();

            em.getTransaction().commit();
            LOG.info( String.format( "Template deleted : %s", template.getTemplateName() ) );
        }
        catch ( Exception ex )
        {
            LOG.error( "Exception deleting template : %s", template.getTemplateName() );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }

            throw new DaoException( ex );
        }
        finally
        {
            em.close();
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
    public Template getTemplate( String templateName, String lxcArch ) throws DaoException
    {
        Template template = null;
        EntityManager em = emf.createEntityManager();
        try
        {
            TypedQuery<Template> query = em.createQuery(
                    "SELECT t FROM Template t WHERE t.pk.templateName = :templateName AND t.pk.lxcArch = :lxcArch",
                    Template.class );
            query.setParameter( "templateName", templateName );
            query.setParameter( "lxcArch", lxcArch );
            List<Template> templates = query.getResultList();
            if ( !templates.isEmpty() )
            {
                template = templates.iterator().next();
            }

            return template;
        }
        catch ( Exception ex )
        {
            throw new DaoException( ex );
        }
        finally
        {
            em.close();
        }
    }


    /**
     * Returns template by name
     *
     * @param templateName - template name
     * @param lxcArch -- lxc arch of template
     * @param md5sum -- lxc md5sum of template
     * @param templateVersion -- lxc templateVersion of template
     *
     * @return {@code Template}
     */
    public Template getTemplate( String templateName, String lxcArch, String md5sum, TemplateVersion templateVersion )
            throws DaoException
    {
        return getTemplate( templateName, lxcArch );
        //TODO this method is temporarily replaced by another one till we find a solution with templates versions
        // Don't delete these
        //        EntityManager entityManager = null;
        //        Template template = null;
        //        try
        //        {
        //            entityManager = entityManagerFactory.createEntityManager();
        //            TypedQuery<Template> query = entityManager
        //                    .createNamedQuery( Template.QUERY_GET_TEMPLATE_BY_NAME_ARCH_MD5_VERSION, Template.class );
        //            query.setParameter( "templateName", templateName );
        //            query.setParameter( "lxcArch", lxcArch );
        //            query.setParameter( "md5sum", md5sum );
        //            query.setParameter( "templateVersion", templateVersion );
        //
        //            List<Template> templates = query.getResultList();
        //            if ( templates.isEmpty() )
        //            {
        //                template = templates.get( 0 );
        //            }
        //
        //            return template;
        //        }
        //        catch ( NoResultException | NonUniqueResultException e )
        //        {
        //            return null;
        //        }
        //        catch ( Exception ex )
        //        {
        //            throw new DaoException( ex );
        //        }
        //        finally
        //        {
        //            if ( entityManager != null )
        //            {
        //                entityManager.close();
        //            }
        //        }
    }


    /**
     * Returns template by name
     *
     * @param templateName - template name
     * @param lxcArch -- lxc arch of template
     * @param templateVersion -- lxc templateVersion of template
     *
     * @return {@code Template}
     */
    public Template getTemplate( String templateName, TemplateVersion templateVersion, String lxcArch )
            throws DaoException
    {
        return getTemplate( templateName, lxcArch );
        //TODO this method is temporarily replaced by another one till we find a solution with templates versions
        // Don't delete these
        //        EntityManager entityManager = null;
        //        Template template = null;
        //        try
        //        {
        //            entityManager = entityManagerFactory.createEntityManager();
        //            TypedQuery<Template> query = entityManager.createQuery(
        //                    "SELECT t FROM Template t WHERE t.pk.templateName = :templateName AND t.pk.lxcArch =
        // :lxcArch AND"
        //                            + " t.pk"
        //                            + ".templateVersion = :templateVersion", Template.class );
        //            query.setParameter( "templateName", templateName );
        //            query.setParameter( "lxcArch", lxcArch );
        //            query.setParameter( "templateVersion", templateVersion.toString() );
        //            List<Template> templates = query.getResultList();
        //            if ( !templates.isEmpty() )
        //            {
        //                template = templates.get( 0 );
        //            }
        //
        //            return template;
        //        }
        //        catch ( Exception ex )
        //        {
        //            throw new DaoException( ex );
        //        }
        //        finally
        //        {
        //            if ( entityManager != null )
        //            {
        //                entityManager.close();
        //            }
        //        }
    }


    /**
     * Returns all registered templates from database
     *
     * @return {@code List<Template>}
     */
    public List<Template> getAllTemplates() throws DaoException
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createNamedQuery( Template.QUERY_GET_ALL, Template.class ).getResultList();
        }
        catch ( Exception ex )
        {
            throw new DaoException( ex );
        }
        finally
        {
            em.close();
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
    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch ) throws DaoException
    {
        try
        {
            Template template = this.getTemplate( parentTemplateName, lxcArch );
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


    /**
     * Returns child templates of supplied parent
     *
     * @param parentTemplateName - name of parent template
     * @param lxcArch - lxc arch of template
     *
     * @return {@code List<Template>}
     */
    public List<Template> getChildTemplates( String parentTemplateName, TemplateVersion templateVersion,
                                             String lxcArch ) throws DaoException
    {
        try
        {
            Template template = this.getTemplate( parentTemplateName, templateVersion, lxcArch );
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
