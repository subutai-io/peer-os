package org.safehaus.subutai.common.protocol.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.api.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by talas on 10/29/14.
 */
public class AgentServiceImpl implements AgentService
{
    private EntityManagerFactory entityManagerFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger( AgentServiceImpl.class.getName() );


    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;
    }


    @Override
    public Agent createAgent( Agent agent )
    {
        if ( agent.getHostname() == null || "".equals( agent.getHostname() ) )
        {
            throw new RuntimeException( "Agent Name is required" );
        }

        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.persist( agent );
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
        return agent;
    }


    @Override
    public Agent getAgent( final long id )
    {
        EntityManager entityManager = null;
        Agent Agent = null;

        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            Agent = entityManager.find( Agent.class, id );
            entityManager.getTransaction().commit();
        }
        catch ( EntityNotFoundException ex )
        {
            LOGGER.error( "Agent not found with id: " + String.valueOf( id ), ex );
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Error in getAgent method", ex );
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
        return Agent;
    }


    @Override
    public List<Agent> getAgents()
    {
        EntityManager entityManager = null;

        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            return entityManager.createNamedQuery( Agent.QUERY_GET_ALL, Agent.class ).getResultList();
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
    public void deleteAgent( final long id )
    {
        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            Agent Agent = entityManager.find( Agent.class, id );
            entityManager.remove( Agent );
            entityManager.getTransaction().commit();
            LOGGER.info( String.format( "Agent deleted with id: %d", id ) );
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Exception deleting Agent with id: %d", id );
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
        LOGGER.warn( "Starting AgentServiceImpl test" );
        if ( entityManagerFactory != null )
        {
            LOGGER.warn( "EntityManagerFactory is connected" );
        }
        Agent agent = new Agent( UUID.randomUUID(), UUID.randomUUID().toString(), "parentHostName", "macAddress",
                new ArrayList<>( Arrays.asList( new String[] { "param1", "param2" } ) ), true, "transportId",
                UUID.randomUUID(), UUID.randomUUID() );
        this.createAgent( agent );
        List<Agent> agents = this.getAgents();
        for ( Agent agent1 : agents )
        {
            LOGGER.warn( "Agent name saved in database: " + agent1.getHostname() );
        }
    }
}
