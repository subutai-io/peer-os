package org.safehaus.subutai.common.protocol.impl;


import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.api.EnvironmentBlueprintService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by talas on 10/30/14.
 */
public class EnvironmentBlueprintServiceImpl implements EnvironmentBlueprintService
{
    private EntityManagerFactory entityManagerFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger( EnvironmentBlueprintServiceImpl.class.getName() );


    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;
    }


    @Override
    public EnvironmentBlueprint createEnvironmentBlueprint( EnvironmentBlueprint blueprint )
    {
        if ( blueprint.getName() == null || "".equals( blueprint.getName() ) )
        {
            throw new RuntimeException( "EnvironmentBlueprint Name is required" );
        }

        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.persist( blueprint );
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
        return blueprint;
    }


    @Override
    public EnvironmentBlueprint getEnvironmentBlueprint( final long id )
    {
        EntityManager entityManager = null;
        EnvironmentBlueprint blueprint = null;

        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            blueprint = entityManager.find( EnvironmentBlueprint.class, id );
            entityManager.getTransaction().commit();
        }
        catch ( EntityNotFoundException ex )
        {
            LOGGER.error( "EnvironmentBlueprint not found with id: " + String.valueOf( id ), ex );
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Error in getEnvironmentBlueprint method", ex );
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
        return blueprint;
    }


    @Override
    public List<EnvironmentBlueprint> getEnvironmentBlueprintsAgents()
    {
        EntityManager entityManager = null;

        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            return entityManager.createNamedQuery( EnvironmentBlueprint.QUERY_GET_ALL, EnvironmentBlueprint.class )
                                .getResultList();
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
    public void deleteEnvironmentBlueprint( final long id )
    {
        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            EnvironmentBlueprint blueprint = entityManager.find( EnvironmentBlueprint.class, id );
            entityManager.remove( blueprint );
            entityManager.getTransaction().commit();
            LOGGER.info( String.format( "EnvironmentBlueprint deleted with id: %d", id ) );
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Exception deleting EnvironmentBlueprint with id: %d", id );
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


    public void test()
    {
        LOGGER.warn( "Starting EnvironmentBlueprintServiceImpl test" );
        EnvironmentBlueprint blueprint =
                new EnvironmentBlueprint( UUID.randomUUID().toString(), "domainName", true, true );
        this.createEnvironmentBlueprint( blueprint );
        List<EnvironmentBlueprint> blueprints = this.getEnvironmentBlueprintsAgents();
        for ( EnvironmentBlueprint environmentBlueprint : blueprints )
        {
            LOGGER.warn( "EnvironmentBlueprint name saved in database: " + environmentBlueprint.getName() );
        }
    }
}
