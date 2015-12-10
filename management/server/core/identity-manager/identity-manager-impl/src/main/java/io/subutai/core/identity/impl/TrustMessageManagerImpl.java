package io.subutai.core.identity.impl;


import java.io.UnsupportedEncodingException;

import org.bouncycastle.openpgp.PGPException;

import com.google.common.collect.Sets;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.identity.api.model.Relation;
import io.subutai.core.identity.api.model.RelationInfo;
import io.subutai.core.identity.api.relation.TrustMessageManager;
import io.subutai.core.identity.impl.model.RelationImpl;
import io.subutai.core.identity.impl.model.RelationInfoImpl;
import io.subutai.core.security.api.SecurityManager;


/**
 * Created by talas on 12/10/15.
 */
public class TrustMessageManagerImpl implements TrustMessageManager
{
    private io.subutai.core.security.api.SecurityManager securityManager;


    public TrustMessageManagerImpl( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    @Override
    public Relation decryptAndVerifyMessage( final String encryptedMessage )
            throws PGPException, UnsupportedEncodingException
    {
        byte[] decrypted = securityManager.getEncryptionTool().decrypt( encryptedMessage.getBytes() );

        String decryptedMessage = new String( decrypted, "UTF-8" );

        //TODO Check signature

        RelationImpl trustRelation = JsonUtil.fromJson( decryptedMessage, RelationImpl.class );

        return trustRelation;
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
                case "type":
                    relationship.setType( value );
                    break;
            }
        }

        return relationship;
    }
}
