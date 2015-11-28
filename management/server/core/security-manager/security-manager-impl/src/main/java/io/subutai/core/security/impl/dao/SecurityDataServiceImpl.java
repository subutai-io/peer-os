package io.subutai.core.security.impl.dao;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.dao.SecurityDataService;
import io.subutai.core.security.api.model.SecretKeyStore;
import io.subutai.core.security.api.model.SecurityKeyIdentity;
import io.subutai.core.security.api.model.SecurityKeyTrust;
import io.subutai.core.security.impl.model.SecretKeyStoreEntity;
import io.subutai.core.security.impl.model.SecurityKeyIdentityEntity;
import io.subutai.core.security.impl.model.SecurityKeyTrustEntity;


/**
 * Implementation of SecurityManagerDAO
 */
public class SecurityDataServiceImpl implements SecurityDataService
{
    private static final Logger LOG = LoggerFactory.getLogger( SecurityDataServiceImpl.class );

    private DaoManager daoManager = null;
    private SecretKeyStoreDAO secretKeyStoreDAO = null;
    private SecurityKeyTrustDAO securityKeyTrustDAO = null;
    private SecurityKeyIdentityDAO securityKeyIdentityDAO = null;


    /******************************************
     *
     */
    public SecurityDataServiceImpl( DaoManager daoManager )
    {
        this.daoManager = daoManager;
        this.secretKeyStoreDAO = new SecretKeyStoreDAO( daoManager );
        this.securityKeyTrustDAO = new SecurityKeyTrustDAO( daoManager );
        this.securityKeyIdentityDAO = new SecurityKeyIdentityDAO( daoManager );
    }


    /******************************************
     *
     */
    @Override
    public void saveKeyIdentityData( final String identityId, final String sKeyId, final String pKeyId, final int type )
    {
        try
        {

            SecurityKeyIdentity securityKeyIdentity = getKeyIdentityData( identityId );

            if ( securityKeyIdentity == null )
            {
                securityKeyIdentity = new SecurityKeyIdentityEntity();
                securityKeyIdentity.setIdentityId( identityId );
                securityKeyIdentity.setType( type );
                securityKeyIdentity.setPublicKeyFingerprint( pKeyId );
                securityKeyIdentity.setSecretKeyFingerprint( sKeyId );
                securityKeyIdentityDAO.persist( securityKeyIdentity );
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
                securityKeyIdentityDAO.update( securityKeyIdentity );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error Saving Identity data", ex );
        }
    }


    /******************************************
     *
     */
    @Override
    public void removeKeyIdentityData( String identityId )
    {
        securityKeyIdentityDAO.remove( identityId );
    }


    /******************************************
     *
     */
    @Override
    public SecurityKeyIdentity getKeyIdentityData( final String identityId )
    {
        return securityKeyIdentityDAO.find( identityId );
    }


    /******************************************
     *
     */
    @Override
    public SecurityKeyIdentity getKeyIdentityDataByFingerprint( final String fingerprint )
    {
        return securityKeyIdentityDAO.findByFingerprint( fingerprint );
    }


    // ********** Secret Key Store ***************
    /******************************************
     *
     */
    @Override
    public SecretKeyStore getSecretKeyData( String fingerprint )
    {
        try
        {
            return secretKeyStoreDAO.find( fingerprint );
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /******************************************
     *
     */
    @Override
    public void saveSecretKeyData( String fingerprint, byte[] data, String pwd, int type )
    {
        try
        {
            SecretKeyStore secretKeyStore = new SecretKeyStoreEntity();

            secretKeyStore.setKeyFingerprint( fingerprint );
            secretKeyStore.setData( data );
            secretKeyStore.setPwd( pwd );
            secretKeyStore.setType( type );

            secretKeyStoreDAO.persist( secretKeyStore );
        }
        catch ( Exception ex )
        {
            LOG.error( " ****** Error Saving Secret key **********", ex );
        }
    }


    /******************************************
     *
     */
    @Override
    public void removeSecretKeyData( String fingerprint )
    {
        try
        {
            secretKeyStoreDAO.remove( fingerprint );
        }
        catch ( Exception ex )
        {
            LOG.error( " ****** Error Removing Secret key **********", ex );
        }
    }


    /*******************************************
     * Trust Data
     */
    @Override
    public void saveKeyTrustData( String sourceFingerprint, String targetFingerprint, int trustLevel )
    {
        try
        {
            SecurityKeyTrust secTrust = new SecurityKeyTrustEntity();
            secTrust.setSourceFingerprint( sourceFingerprint );
            secTrust.setTargetFingerprint( targetFingerprint );
            secTrust.setLevel( trustLevel );

            securityKeyTrustDAO.persist( secTrust );
        }
        catch ( Exception ex )
        {
        }
    }


    /******************************************
     *
     */
    @Override
    public void updateKeyTrustData( SecurityKeyTrust securityKeyTrust )
    {
        securityKeyTrustDAO.update( securityKeyTrust );
    }


    /******************************************
     *
     */
    @Override
    public void removeKeyTrustData( long trustDataid )
    {
        securityKeyTrustDAO.remove( trustDataid );
    }


    /******************************************
     *
     */
    @Override
    public void removeKeyTrustData( String sourceFingerprint )
    {
        securityKeyTrustDAO.removeBySourceId( sourceFingerprint );
    }


    /******************************************
     *
     */
    @Override
    public void removeKeyTrustData( String sourceFingerprint, String targetFingerprint )
    {
        securityKeyTrustDAO.removeBySourceId( sourceFingerprint, targetFingerprint );
    }


    /******************************************
     *
     */
    @Override
    public SecurityKeyTrust getKeyTrustData( long id )
    {
        return securityKeyTrustDAO.find( id );
    }


    /******************************************
     *
     */
    @Override
    public SecurityKeyTrust getKeyTrustData( String sourceFingerprint, String targetFingerprint)
    {
        return securityKeyTrustDAO.findBySourceId( sourceFingerprint, targetFingerprint );
    }


    /******************************************
     *
     */
    @Override
    public List<SecurityKeyTrust> getKeyTrustData( String sourceFingerprint)
    {
        return securityKeyTrustDAO.findBySourceId( sourceFingerprint );
    }
}
