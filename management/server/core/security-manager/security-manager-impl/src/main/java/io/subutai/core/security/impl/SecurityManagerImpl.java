package io.subutai.core.security.impl;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.CertificateManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.crypto.KeyStoreManager;
import io.subutai.core.security.api.dao.SecurityDataService;
import io.subutai.core.security.api.jetty.HttpContextManager;
import io.subutai.core.security.api.model.TrustItem;
import io.subutai.core.security.api.model.TrustRelation;
import io.subutai.core.security.api.model.TrustRelationship;
import io.subutai.core.security.impl.crypto.CertificateManagerImpl;
import io.subutai.core.security.impl.crypto.EncryptionToolImpl;
import io.subutai.core.security.impl.crypto.KeyManagerImpl;
import io.subutai.core.security.impl.crypto.KeyStoreManagerImpl;
import io.subutai.core.security.impl.dao.SecurityDataServiceImpl;
import io.subutai.core.security.impl.jetty.HttpContextManagerImpl;
import io.subutai.core.security.impl.model.SecurityKeyData;
import io.subutai.core.security.impl.model.TrustRelationshipImpl;


/**
 * Implementation of SecurityManager
 */
public class SecurityManagerImpl implements SecurityManager
{
    private static final Logger LOG = LoggerFactory.getLogger( SecurityManagerImpl.class );

    private KeyManager keyManager = null;
    private DaoManager daoManager = null;
    private EncryptionTool encryptionTool = null;
    private SecurityDataService securityDataService = null;
    private KeyServer keyServer = null;
    private SecurityKeyData keyData = null;
    private KeyStoreManager keyStoreManager = null;
    private CertificateManager certificateManager = null;
    private HttpContextManager httpContextManager;


    /* *****************************
     *
     */
    public SecurityManagerImpl( String ownerPublicKeyringFile, String secretKeyringFile, String publicKeyringFile,
                                String secretKeyringPwd, Object provider )
    {
        keyData = new SecurityKeyData();

        keyData.setOwnerPublicKeyringFile( ownerPublicKeyringFile );
        keyData.setSecretKeyringFile( secretKeyringFile );
        keyData.setPublicKeyringFile( publicKeyringFile );
        keyData.setSecretKeyringPwd( secretKeyringPwd );
        keyData.setJsonProvider( provider );

        httpContextManager = new HttpContextManagerImpl();
    }


    /* *****************************
     *
     */
    public void init()
    {
        securityDataService = new SecurityDataServiceImpl( daoManager );
        keyManager = new KeyManagerImpl( securityDataService, keyServer, keyData );
        encryptionTool = new EncryptionToolImpl( ( KeyManagerImpl ) keyManager );
        keyStoreManager = new KeyStoreManagerImpl();
        certificateManager = new CertificateManagerImpl();
    }


    /* *****************************
     *
     */
    public void destroy()
    {

    }


    /**
     * *****************************
     */
    @Override
    public KeyManager getKeyManager()
    {
        return keyManager;
    }


    /* *****************************
     */
    public void setKeyManager( KeyManager keyManager )
    {
        this.keyManager = keyManager;
    }


    /* ****************************
     *
     */
    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *****************************
     *
     */
    public SecurityDataService getSecurityManagerDAO()
    {
        return securityDataService;
    }


    /* *****************************
     *
     */
    public void setSecurityManagerDAO( final SecurityDataService securityDataService )
    {
        this.securityDataService = securityDataService;
    }


    /* *****************************
     *
     */
    public KeyServer getKeyServer()
    {
        return keyServer;
    }


    /* *****************************
     *
     */
    public void setKeyServer( final KeyServer keyServer )
    {
        this.keyServer = keyServer;
    }


    /**
     * *****************************
     */
    public EncryptionTool getEncryptionTool()
    {
        return encryptionTool;
    }


    /* *****************************
     *
     */
    public void setEncryptionTool( final EncryptionTool encryptionTool )
    {
        this.encryptionTool = encryptionTool;
    }


    /* *****************************
     *
     */
    public SecurityKeyData getSecurityKeyData()
    {
        return keyData;
    }


    /* *****************************
     *
     */
    @Override
    public KeyStoreManager getKeyStoreManager()
    {
        return keyStoreManager;
    }


    /* *****************************
     *
     */
    public void setKeyStoreManager( final KeyStoreManager keyStoreManager )
    {
        this.keyStoreManager = keyStoreManager;
    }


    /* *****************************
     *
     */
    @Override
    public CertificateManager getCertificateManager()
    {
        return certificateManager;
    }


    /* *****************************
     *
     */
    public void setCertificateManager( final CertificateManager certificateManager )
    {
        this.certificateManager = certificateManager;
    }


    /* *****************************
     *
     */
    @Override
    public HttpContextManager getHttpContextManager()
    {
        return httpContextManager;
    }


    @Override
    public void createTrustRelationship( final Map<String, String> relationshipProp )
    {
        securityDataService.createTrustRelationship( relationshipProp );
    }


    @Override
    public boolean isRelationValid( final String sourceId, final String sourcePath, final String objectId,
                                    final String objectPath, String statement )
    {
        TrustItem source = securityDataService.getTrustItem( sourceId, sourcePath );
        TrustItem object = securityDataService.getTrustItem( objectId, objectPath );

        TrustRelation trustRelation = securityDataService.getTrustRelationBySourceObject( source, object );

        TrustRelationship parsedRelationship = buildTrustRelationFromCondition( statement );
        if ( trustRelation != null )
        {
            return trustRelation.getRelationship().equals( parsedRelationship );
        }
        else
        {
            return false;
        }
    }


    /**
     * Relation condition is simple string presentation of propertyKey=propertyValue=condition with each line new
     * condition is declared. Property values should be comparative objects so that with conditions it would be possible
     * to identify which condition has greater scope
     */
    private TrustRelationship buildTrustRelationFromCondition( String relationCondition )
    {
        TrustRelationshipImpl relationship = new TrustRelationshipImpl();
        String[] conditions = relationCondition.split( "\n" );

        for ( final String condition : conditions )
        {
            String[] parsed = condition.split( "=" );
            String key = parsed[0];
            String value = parsed[1];

            switch ( key )
            {
                case "trustLevel":
                    relationship.setTrustLevel( value );
                    break;
                case "scope":
                    relationship.setScope( value );
                    break;
                case "action":
                    relationship.setAction( value );
                    break;
                case "ttl":
                    relationship.setTtl( value );
                    break;
                case "type":
                    relationship.setType( value );
                    break;
            }
        }

        return relationship;
    }
}
