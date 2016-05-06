package io.subutai.core.environment.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;

import io.subutai.common.environment.Environment;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class EnvironmentServiceImpl implements EnvironmentService
{

    private EntityManager em;


    public void setEntityManager( EntityManager em )
    {
        this.em = em;
    }


    @Override
    public EnvironmentImpl find( final String id )
    {
        return em.find( EnvironmentImpl.class, id );
    }


    @Override
    public List<EnvironmentImpl> getAll()
    {
        return em.createQuery( "select e from EnvironmentImpl e", EnvironmentImpl.class ).getResultList();
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
        em.remove( em.getReference( EnvironmentImpl.class, id ) );
    }


    @Override
    public EnvironmentImpl merge( final EnvironmentImpl item )
    {

        EnvironmentImpl environment = em.merge( item );

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
