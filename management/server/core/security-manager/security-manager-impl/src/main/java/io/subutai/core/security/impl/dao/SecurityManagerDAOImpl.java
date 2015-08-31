package io.subutai.core.security.impl.dao;


import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.dao.SecurityManagerDAO;
import io.subutai.core.security.api.model.SecurityKeyIdentity;
import io.subutai.core.security.impl.model.SecurityKeyIdentityEntity;


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
    public SecurityKeyIdentity getKeyIdentityData( String hostId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            SecurityKeyIdentity securityKeyIdentity = em.find( SecurityKeyIdentityEntity.class, hostId );

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
    public String getKeyFingerprint( String hostId )
    {
        SecurityKeyIdentity securityKeyIdentity = getKeyIdentityData( hostId );

        if(securityKeyIdentity!=null)
            return securityKeyIdentity.getKeyFingerprint();
        else
            return "";
    }


    /******************************************
     *
     */
    @Override
    public void saveKeyIdentityData( String hostId ,String keyId, short type )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            SecurityKeyIdentity securityKeyIdentity = new SecurityKeyIdentityEntity();

            daoManager.startTransaction( em );

            securityKeyIdentity.setHostId( hostId );
            securityKeyIdentity.setKeyFingerprint( keyId );
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
    public void removeKeyIdentityData( String hostId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            em.remove( getKeyIdentityData(hostId ));

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
