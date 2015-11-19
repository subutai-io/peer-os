package io.subutai.core.security.impl.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.dao.SecurityDataService;
import io.subutai.core.security.api.model.SecretKeyStore;
import io.subutai.core.security.api.model.SecurityKeyIdentity;
import io.subutai.core.security.impl.model.SecretKeyStoreEntity;
import io.subutai.core.security.impl.model.SecurityKeyIdentityEntity;


/**
 * Implementation of SecurityManagerDAO
 */
public class SecurityDataServiceImpl implements SecurityDataService
{
    private static final Logger LOG = LoggerFactory.getLogger( SecurityDataServiceImpl.class );

    private DaoManager daoManager = null;
    private SecretKeyStoreDAO secretKeyStoreDAO = null;
    private SecurityKeyTrustDAO SecurityKeyTrustDAO = null;
    private SecurityKeyIdentityDAO securityKeyIdentityDAO = null;


    /******************************************
     *
     */
    public SecurityDataServiceImpl( DaoManager daoManager )
    {
        this.daoManager = daoManager;
        this.secretKeyStoreDAO = new SecretKeyStoreDAO( daoManager );
        this.SecurityKeyTrustDAO = new SecurityKeyTrustDAO( daoManager );
        this.securityKeyIdentityDAO = new SecurityKeyIdentityDAO( daoManager );
    }


    /******************************************
     *
     */
    @Override
    public void saveKeyIdentityData( final String hostId, final String sKeyId, final String pKeyId, final short type )
    {
        try
        {

            SecurityKeyIdentity securityKeyIdentity = getKeyIdentityData( hostId );

            if ( securityKeyIdentity == null )
            {
                securityKeyIdentity = new SecurityKeyIdentityEntity();
                securityKeyIdentity.setHostId( hostId );
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
        catch(Exception ex)
        {
            LOG.error( "Error Saving Identity data",ex );
        }
    }


    /******************************************
     *
     */
    @Override
    public void removeKeyIdentityData( String hostId )
    {
        securityKeyIdentityDAO.remove( hostId );
    }


    /******************************************
     *
     */
    @Override
    public SecurityKeyIdentity getKeyIdentityData( final String hostId )
    {
        return securityKeyIdentityDAO.find( hostId );
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
    public void saveSecretKeyData( String fingerprint, byte[] data, String pwd, short type )
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
            LOG.error( " ****** Error Saving Secret key **********",ex);
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
            LOG.error( " ****** Error Removing Secret key **********",ex);
        }
    }



}
