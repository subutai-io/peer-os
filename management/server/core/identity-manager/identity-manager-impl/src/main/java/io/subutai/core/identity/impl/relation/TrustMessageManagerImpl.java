package io.subutai.core.identity.impl.relation;


import java.io.UnsupportedEncodingException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import com.google.common.collect.Sets;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.identity.api.exception.RelationVerificationException;
import io.subutai.core.identity.api.model.Relation;
import io.subutai.core.identity.api.model.RelationInfo;
import io.subutai.core.identity.api.relation.TrustMessageManager;
import io.subutai.core.identity.impl.model.RelationImpl;
import io.subutai.core.identity.impl.model.RelationInfoImpl;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


/**
 * Created by talas on 12/10/15.
 */
public class TrustMessageManagerImpl implements TrustMessageManager
{
    private SecurityManager securityManager;


    public TrustMessageManagerImpl( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    @Override
    public Relation decryptAndVerifyMessage( final String encryptedMessage, final String secretKeyId )
            throws PGPException, UnsupportedEncodingException, RelationVerificationException
    {
        KeyManager keyManager = securityManager.getKeyManager();
        EncryptionTool encryptionTool = securityManager.getEncryptionTool();

        PGPSecretKeyRing secretKeyRing = keyManager.getSecretKeyRing( secretKeyId );

        byte[] decrypted = encryptionTool.decrypt( encryptedMessage.getBytes(), secretKeyRing, "" );

        String decryptedMessage = new String( decrypted, "UTF-8" );

        RelationImpl relation = JsonUtil.fromJson( decryptedMessage, RelationImpl.class );

        PGPPublicKey publicKey = keyManager.getPublicKey( relation.getKeyId() );
        if ( !encryptionTool.verify( encryptedMessage.getBytes(), publicKey ) )
        {
            throw new RelationVerificationException( "Relation message verification failed." );
        }

        return relation;
    }


    @Override
    public String authenticateSource( final Relation trustMessage )
    {
        return "";
    }


    @Override
    public boolean verifyMessageSource( final Relation trustMessage, final String signature, String sourceFingerprint )
    {
        return false;
    }


    /**
     * Relation condition is simple string presentation of propertyKey=propertyValue=condition with each line new
     * condition is declared. Property values should be comparative objects so that with conditions it would be possible
     * to identify which condition has greater scope
     */
    @Override
    public RelationInfo serializeMessage( final String rawRelationship )
    {
        //TODO parse raw relationship
        RelationInfoImpl relationship = new RelationInfoImpl();
        String[] conditions = rawRelationship.split( "\n" );

        for ( final String condition : conditions )
        {
            String[] parsed = condition.split( "=" );
            String key = parsed[0];
            String value = parsed[1];

            switch ( key )
            {
                case "context":
                    relationship.setContext( value );
                    break;
                case "operation":
                    relationship.setOperation( Sets.newHashSet( value.split( "," ) ) );
                    break;
                case "ownership":
                    relationship.setOwnershipLevel( Integer.valueOf( value ) );
                    break;
            }
        }

        return relationship;
    }
}
