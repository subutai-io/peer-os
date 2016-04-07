package io.subutai.core.localpeer.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.exception.DaoException;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.util.IPUtil;
import io.subutai.core.localpeer.impl.entity.NetworkResourceEntity;


public class NetworkResourceDaoImpl implements NetworkResourceDao<NetworkResourceEntity, String>
{
    private final EntityManagerFactory entityManagerFactory;


    public NetworkResourceDaoImpl( final EntityManagerFactory entityManagerFactory )
    {
        Preconditions.checkNotNull( entityManagerFactory );

        this.entityManagerFactory = entityManagerFactory;
    }


    @Override
    public synchronized void create( final NetworkResourceEntity networkResource ) throws DaoException
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try
        {
            entityManager.getTransaction().begin();

            entityManager.persist( networkResource );

            entityManager.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( entityManager.getTransaction().isActive() )
            {
                entityManager.getTransaction().rollback();
            }

            throw new DaoException( e );
        }
        finally
        {
            entityManager.close();
        }
    }


    @Override
    public synchronized NetworkResourceEntity read( final String environmentId ) throws DaoException
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        NetworkResourceEntity networkResource;

        try
        {
            entityManager.getTransaction().begin();

            networkResource = entityManager.find( NetworkResourceEntity.class, environmentId );

            entityManager.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( entityManager.getTransaction().isActive() )
            {
                entityManager.getTransaction().rollback();
            }

            throw new DaoException( e );
        }
        finally
        {
            entityManager.close();
        }

        return networkResource;
    }


    @Override
    public synchronized List<NetworkResourceEntity> readAll() throws DaoException
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        List<NetworkResourceEntity> networkResources;

        try
        {
            entityManager.getTransaction().begin();

            networkResources =
                    entityManager.createQuery( "select h from NetworkResourceEntity h", NetworkResourceEntity.class )
                                 .getResultList();

            entityManager.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( entityManager.getTransaction().isActive() )
            {
                entityManager.getTransaction().rollback();
            }

            throw new DaoException( e );
        }
        finally
        {
            entityManager.close();
        }

        return networkResources;
    }


    @Override
    public synchronized NetworkResourceEntity update( NetworkResourceEntity networkResource ) throws DaoException
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try
        {
            entityManager.getTransaction().begin();

            networkResource = entityManager.merge( networkResource );

            entityManager.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( entityManager.getTransaction().isActive() )
            {
                entityManager.getTransaction().rollback();
            }

            throw new DaoException( e );
        }
        finally
        {
            entityManager.close();
        }

        return networkResource;
    }


    @Override
    public synchronized void delete( NetworkResourceEntity networkResource ) throws DaoException
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try
        {
            entityManager.getTransaction().begin();

            networkResource = entityManager.merge( networkResource );

            entityManager.remove( networkResource );

            entityManager.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( entityManager.getTransaction().isActive() )
            {
                entityManager.getTransaction().rollback();
            }

            throw new DaoException( e );
        }
        finally
        {
            entityManager.close();
        }
    }


    public NetworkResourceEntity find( final NetworkResource networkResource ) throws DaoException
    {
        Preconditions.checkNotNull( networkResource );

        List<NetworkResourceEntity> networkResources = readAll();

        String containerSubnet = IPUtil.getNetworkAddress( networkResource.getContainerSubnet() );
        String p2pSubnet = IPUtil.getNetworkAddress( networkResource.getP2pSubnet() );

        for ( NetworkResourceEntity netResource : networkResources )
        {
            if ( networkResource.getEnvironmentId().equalsIgnoreCase( netResource.getEnvironmentId() ) )
            {
                return netResource;
            }
            if ( containerSubnet.equalsIgnoreCase( netResource.getContainerSubnet() ) )
            {
                return netResource;
            }
            if ( p2pSubnet.equalsIgnoreCase( netResource.getP2pSubnet() ) )
            {
                return netResource;
            }
            if ( networkResource.getVni() == netResource.getVni() )
            {
                return netResource;
            }
        }

        return null;
    }
}
