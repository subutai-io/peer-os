package io.subutai.core.security.impl.dao;


import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

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
    public String getPublicKeyFingerprint( String hostId )
    {
        SecurityKeyIdentity securityKeyIdentity = getKeyIdentityData( hostId );

        if(securityKeyIdentity!=null)
            return securityKeyIdentity.getPublicKeyFingerprint();
        else
            return "";
    }


    /******************************************
     * Get SecretKey from DB
     */
    @Override
    public String getSecretKeyFingerprint( String hostId )
    {
        SecurityKeyIdentity securityKeyIdentity = getKeyIdentityData( hostId );

        if(securityKeyIdentity!=null)
            return securityKeyIdentity.getSecretKeyFingerprint();
        else
            return "";
    }



    /******************************************
     *
     */
    @Override
    public void saveKeyIdentityData( String hostId ,String sKeyId,String pKeyId, short type )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {

            SecurityKeyIdentity securityKeyIdentity = getKeyIdentityData( hostId );

            if(securityKeyIdentity == null)
            {
                securityKeyIdentity = new SecurityKeyIdentityEntity();
                securityKeyIdentity.setHostId( hostId );
                securityKeyIdentity.setType( type );
                securityKeyIdentity.setPublicKeyFingerprint( pKeyId );
                securityKeyIdentity.setSecretKeyFingerprint( sKeyId );
            }
            else
            {
                if ( Strings.isNullOrEmpty( pKeyId ) )
                {
                    securityKeyIdentity.setSecretKeyFingerprint( sKeyId );
                }

                if ( Strings.isNullOrEmpty( sKeyId ) )
                {
                    securityKeyIdentity.setPublicKeyFingerprint( pKeyId );
                }
            }

            daoManager.startTransaction( em );
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
