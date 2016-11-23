package io.subutai.core.environment.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;

import io.subutai.common.environment.Environment;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class EnvironmentServiceImpl implements EnvironmentService
{

    private EntityManager em;


    public void setEntityManager( EntityManager em )
    {
        this.em = em;
    }


    @Override
    public LocalEnvironment find( final String id )
    {
        return em.find( LocalEnvironment.class, id );
    }


    @Override
    public List<LocalEnvironment> getAll()
    {
        return em.createQuery( "select e from LocalEnvironment e", LocalEnvironment.class ).getResultList();
    }


    @Override
    public void persist( final Environment item )
    {
        em.persist( item );

        em.flush();

        em.refresh( item );
    }


    @Override
    public void remove( final String id )
    {
        em.remove( em.getReference( LocalEnvironment.class, id ) );
    }


    @Override
    public LocalEnvironment merge( final LocalEnvironment item )
    {

        LocalEnvironment environment = em.merge( item );

        em.flush();

        em.refresh( environment );

        return environment;
    }


    @Override
    public EnvironmentContainerImpl mergeContainer( EnvironmentContainerImpl item )
    {
        EnvironmentContainerImpl environmentContainer = em.merge( item );

        em.flush();

        em.refresh( environmentContainer );

        return environmentContainer;
    }
}
