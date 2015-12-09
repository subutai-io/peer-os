package io.subutai.core.security.impl.dao;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.dao.SecurityDataService;
import io.subutai.core.security.api.model.SecretKeyStore;
import io.subutai.core.security.api.model.SecurityKey;
import io.subutai.core.security.api.model.SecurityKeyTrust;
import io.subutai.core.security.impl.model.SecretKeyStoreEntity;
import io.subutai.core.security.impl.model.SecurityKeyEntity;
import io.subutai.core.security.impl.model.SecurityKeyTrustEntity;
import io.subutai.core.security.impl.model.TrustItem;
import io.subutai.core.security.impl.model.TrustRelation;
import io.subutai.core.security.impl.model.TrustRelationship;


/**
 * Implementation of SecurityManagerDAO
 */
public class SecurityDataServiceImpl implements SecurityDataService
{
    private static final Logger LOG = LoggerFactory.getLogger( SecurityDataServiceImpl.class );

    private DaoManager daoManager = null;
    private SecretKeyStoreDAO secretKeyStoreDAO = null;
    private SecurityKeyTrustDAO securityKeyTrustDAO = null;
    private SecurityKeyDAO securityKeyDAO = null;
    private TrustRelationDAO trustRelationDAO = null;


    /******************************************
     *
     */
    public SecurityDataServiceImpl( DaoManager daoManager )
    {
        this.daoManager = daoManager;
        this.secretKeyStoreDAO = new SecretKeyStoreDAO( daoManager );
        this.securityKeyTrustDAO = new SecurityKeyTrustDAO( daoManager );
        this.securityKeyDAO = new SecurityKeyDAO( daoManager );
        this.trustRelationDAO = new TrustRelationDAO( daoManager );
    }


    /******************************************
     *
     */
    @Override
    public void saveKeyData( final String identityId, final String sKeyId, final String pKeyId, final int type )
    {
        try
        {

            SecurityKey SecurityKey = getKeyData( identityId );

            if ( SecurityKey == null )
            {
                SecurityKey = new SecurityKeyEntity();
                SecurityKey.setIdentityId( identityId );
                SecurityKey.setType( type );
                SecurityKey.setPublicKeyFingerprint( pKeyId );
                SecurityKey.setSecretKeyFingerprint( sKeyId );
                securityKeyDAO.persist( SecurityKey );
            }
            else
            {
                if ( Strings.isNullOrEmpty( pKeyId ) )
                {
                    SecurityKey.setSecretKeyFingerprint( sKeyId );
                }

                if ( Strings.isNullOrEmpty( sKeyId ) )
                {
                    SecurityKey.setPublicKeyFingerprint( pKeyId );
                }
                securityKeyDAO.update( SecurityKey );
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
    public void removeKeyData( String identityId )
    {
        securityKeyDAO.remove( identityId );
    }


    /******************************************
     *
     */
    @Override
    public SecurityKey getKeyData( final String identityId )
    {
        return securityKeyDAO.find( identityId );
    }


    /******************************************
     *
     */
    @Override
    public SecurityKey getKeyDataByFingerprint( final String fingerprint )
    {
        return securityKeyDAO.findByFingerprint( fingerprint );
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
    public SecurityKeyTrust saveKeyTrustData( String sourceFingerprint, String targetFingerprint, int trustLevel )
    {
        SecurityKeyTrust secTrust = null;

        try
        {
            secTrust = new SecurityKeyTrustEntity();
            secTrust.setSourceFingerprint( sourceFingerprint );
            secTrust.setTargetFingerprint( targetFingerprint );
            secTrust.setLevel( trustLevel );

            securityKeyTrustDAO.persist( secTrust );
        }
        catch ( Exception ex )
        {
            return null;
        }

        return secTrust;
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
    public void removeKeyAllTrustData( String fingerprint )
    {
        securityKeyTrustDAO.removeAll( fingerprint );
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
    public SecurityKeyTrust getKeyTrustData( String sourceFingerprint, String targetFingerprint )
    {
        return securityKeyTrustDAO.findBySourceId( sourceFingerprint, targetFingerprint );
    }


    /******************************************
     *
     */
    @Override
    public List<SecurityKeyTrust> getKeyTrustData( String sourceFingerprint )
    {
        return securityKeyTrustDAO.findBySourceId( sourceFingerprint );
    }


    @Override
    public void createTrustRelationship( final Map<String, String> relationshipProp )
    {
        TrustItem source = new TrustItem( relationshipProp.get( "sourceId" ), relationshipProp.get( "sourceClass" ) );
        TrustItem target = new TrustItem( relationshipProp.get( "targetId" ), relationshipProp.get( "targetClass" ) );
        TrustItem object = new TrustItem( relationshipProp.get( "objectId" ), relationshipProp.get( "objectClass" ) );

        trustRelationDAO.update( source );
        trustRelationDAO.update( target );
        trustRelationDAO.update( object );

        TrustRelationship trustRelationship =
                new TrustRelationship( relationshipProp.get( "trustLevel" ), relationshipProp.get( "scope" ),
                        relationshipProp.get( "action" ), relationshipProp.get( "ttl" ),
                        relationshipProp.get( "type" ) );

        TrustRelation trustRelation = new TrustRelation( source, target, object, trustRelationship );

        trustRelationDAO.update( trustRelation );
    }
}
