package io.subutai.core.security.impl.dao;


import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.dao.SecurityManagerDAO;
import io.subutai.core.security.api.model.SecurityKeyIdentity;
import io.subutai.core.security.impl.model.SecurityKeyIndetityEntity;


/**
 * Implementation of SecurityManagerDAO
 */
public class SecurityManagerDAOImpl implements SecurityManagerDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( SecurityManagerDAOImpl.class );

    private DaoManager daoManager = null;


    /******************************************
     *
     */
    public SecurityManagerDAOImpl( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /******************************************
     * Get Security KeyId from DB
     */
    @Override
    public SecurityKeyIdentity getSecurityKeyIdentity( String hostId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            SecurityKeyIdentity securityKeyIdentity = em.find( SecurityKeyIndetityEntity.class, hostId );

            return securityKeyIdentity;
        }
        catch ( Exception ex )
        {
            LOG.error( "SecurityManagerDAOImpl getSecurityKeyId:" + ex.toString() );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /******************************************
     * Get PublicKey from DB
     */
    @Override
    public String getKeyId( String hostId )
    {
        SecurityKeyIdentity securityKeyIdentity = getSecurityKeyIdentity( hostId );

        if(securityKeyIdentity!=null)
            return securityKeyIdentity.getKeyId();
        else
            return "";
    }


    /******************************************
     *
     */
    @Override
    public void saveKey( String hostId ,String keyId, short type )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            SecurityKeyIdentity securityKeyIdentity = new SecurityKeyIndetityEntity();

            daoManager.startTransaction( em );

            securityKeyIdentity.setHostId( hostId );
            securityKeyIdentity.setKeyId( keyId );
            securityKeyIdentity.setType( type );

            em.merge( securityKeyIdentity );

            daoManager.commitTransaction( em );

        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );
            LOG.error( "SecurityManagerDAOImpl saveKey:" + ex.toString() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /******************************************
     *
     */
    @Override
    public void removeKey( String hostId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            em.remove( getSecurityKeyIdentity(hostId ));

            daoManager.commitTransaction( em );
        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );
            LOG.error( "SecurityManagerDAOImpl removeKey:" + ex.toString() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }

}
