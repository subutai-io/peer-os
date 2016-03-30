package io.subutai.core.localpeer.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.exception.DaoException;
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


    @Override
    public synchronized NetworkResourceEntity findByVni( long vni ) throws DaoException
    {
        List<NetworkResourceEntity> networkResources = readAll();

        for ( NetworkResourceEntity networkResource : networkResources )
        {
            if ( vni == networkResource.getVni() )
            {
                return networkResource;
            }
        }

        return null;
    }


    @Override
    public NetworkResourceEntity findByP2pSubnet( String p2pSubnet ) throws DaoException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pSubnet ) );

        List<NetworkResourceEntity> networkResources = readAll();

        for ( NetworkResourceEntity networkResource : networkResources )
        {
            if ( p2pSubnet.equalsIgnoreCase( networkResource.getP2pSubnet() ) )
            {
                return networkResource;
            }
        }

        return null;
    }


    @Override
    public NetworkResourceEntity findByContainerSubnet( String containerSubnet ) throws DaoException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerSubnet ) );

        List<NetworkResourceEntity> networkResources = readAll();

        for ( NetworkResourceEntity networkResource : networkResources )
        {
            if ( containerSubnet.equalsIgnoreCase( networkResource.getP2pSubnet() ) )
            {
                return networkResource;
            }
        }

        return null;
    }


    @Override
    public NetworkResourceEntity find( final NetworkResourceEntity networkResource ) throws DaoException
    {
        Preconditions.checkNotNull( networkResource );

        List<NetworkResourceEntity> networkResources = readAll();

        for ( NetworkResourceEntity netResource : networkResources )
        {
            if ( networkResource.getEnvironmentId().equalsIgnoreCase( netResource.getEnvironmentId() ) )
            {
                return netResource;
            }
            if ( networkResource.getContainerSubnet().equalsIgnoreCase( netResource.getContainerSubnet() ) )
            {
                return netResource;
            }
            if ( networkResource.getP2pSubnet().equalsIgnoreCase( netResource.getP2pSubnet() ) )
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
