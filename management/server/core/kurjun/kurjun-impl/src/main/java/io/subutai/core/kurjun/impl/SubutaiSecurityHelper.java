package io.subutai.core.kurjun.impl;


import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import ai.subut.kurjun.common.service.KurjunContext;
import ai.subut.kurjun.metadata.common.subutai.TemplateId;
import io.subutai.common.security.objects.Ownership;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.kurjun.api.template.TemplateRepository;
import io.subutai.core.kurjun.impl.model.SharedTemplateInfo;
import io.subutai.core.kurjun.impl.model.TemplateAccess;
import io.subutai.core.object.relation.api.RelationManager;
import io.subutai.core.object.relation.api.model.Relation;
import io.subutai.core.object.relation.api.model.RelationInfo;
import io.subutai.core.object.relation.api.model.RelationInfoMeta;
import io.subutai.core.object.relation.api.model.RelationMeta;


/**
 * Helper class that uses Subutai API for template security access
 *
 */
class SubutaiSecurityHelper
{

    private static final Logger LOGGER = LoggerFactory.getLogger( SubutaiSecurityHelper.class );
    
    private static final String TEMPLATE_ACCESS_CLASS = TemplateAccess.class.getSimpleName();

    private final IdentityManager subutaiIdentityManager;

    private final io.subutai.core.security.api.SecurityManager securityManager;

    private final RelationManager relationManager;


    SubutaiSecurityHelper( IdentityManager identityManager, RelationManager relationManager,
            io.subutai.core.security.api.SecurityManager securityManager )
    {
        Objects.requireNonNull( identityManager );
        Objects.requireNonNull( securityManager );
        this.subutaiIdentityManager = identityManager;
        this.securityManager = securityManager;
        this.relationManager = relationManager;
    }


    List<String> getUserFingerprints()
    {
        List<String> fprints = new ArrayList<>();
        List<User> users = subutaiIdentityManager.getAllUsers();

        for ( User user : users )
        {
            String keyId = user.getSecurityKeyId();
            if ( keyId != null && !keyId.trim().isEmpty() )
            {
                fprints.add( getUserFingerprint( keyId ) );
            }
        }
        return fprints;
    }
    
  
    boolean isUserHasKeyId( String fprint )
    {
        User user = subutaiIdentityManager.getUserByFingerprint( fprint.toUpperCase( Locale.US ) );
        return user != null && user.getSecurityKeyId() != null && !user.getSecurityKeyId().isEmpty();
    }


    String getActiveUserFingerprint()
    {
        String keyId = subutaiIdentityManager.getActiveUser().getSecurityKeyId();
        return getUserFingerprint( keyId );
    }


    String getUserFingerprintByUsername( String username )
    {
        try
        {
            User user = subutaiIdentityManager.getUserByUsername( username );
            String keyId = user.getSecurityKeyId();
            return getUserFingerprint( keyId );
        }
        catch ( NullPointerException e )
        {
            throw new IllegalArgumentException( "Failed to get user fingerprint for username " + username );
        }
    }


    Long getUserId( String userFingerprint )
    {
        try
        {
            User user = subutaiIdentityManager.getUserByFingerprint( userFingerprint.toUpperCase( Locale.US ) );
            return user.getId();
        }
        catch ( Exception e )
        {
            LOGGER.warn( "Failed to get user with fingerprint " + userFingerprint, e );
            return null;
        }
    }


    String getUserName( String userFingerprint )
    {
        try
        {
            User user = subutaiIdentityManager.getUserByFingerprint( userFingerprint.toUpperCase( Locale.US ) );
            return user.getUserName();
        }
        catch ( Exception e )
        {
            LOGGER.warn( "Failed to get user with fingerprint " + userFingerprint, e );
            return null;
        }
    }


    void grantOwnerPermissions( TemplateId templateId, String ownerUserFingerprint )
    {
        UserDelegate ud = getUserDelegate( ownerUserFingerprint );
        TemplateAccess ta = new TemplateAccess( templateId );
        destroyRelationChain( ta, ud, ud );
        buildRelationChain( ta, ud, ud, true, true, true, true );
    }


    void grantReadPermissionFromActiveUser( TemplateId templateId, String toUserFingerprint )
    {
        if ( !getActiveUserFingerprint().equals( templateId.getOwnerFprint() ) )
        {
            throw new IllegalArgumentException( "Template can be shared by its owner only" );
        }

        UserDelegate from = getUserDelegate( getActiveUserFingerprint() );
        UserDelegate to = getUserDelegate( toUserFingerprint );

        TemplateAccess ta = new TemplateAccess( templateId );
        destroyRelationChain( ta, from, to );
        buildRelationChain( ta, from, to, true, false, false, false );
    }


    void revokeReadPermission( TemplateId templateId, String toUserFingerprint )
    {
        if ( !getActiveUserFingerprint().equals( templateId.getOwnerFprint() ) )
        {
            throw new IllegalArgumentException( "Template sharing can be revoked by its owner only" );
        }

        UserDelegate from = getUserDelegate( getActiveUserFingerprint() );
        UserDelegate to = getUserDelegate( toUserFingerprint );

        TemplateAccess templateAccess = new TemplateAccess( templateId );
        destroyRelationChain( templateAccess, from, to );
    }


    private void buildRelationChain( TemplateAccess templateAccess, UserDelegate from, UserDelegate to,
            boolean canRead, boolean canWrite, boolean canUpdate, boolean canDelete )
    {
        RelationMeta relationMeta = new RelationMeta();
        relationMeta.setSource( from );
        relationMeta.setTarget( to );
        relationMeta.setObject( templateAccess );

        RelationInfoMeta relationInfoMeta
                = new RelationInfoMeta( canRead, canWrite, canUpdate, canDelete, Ownership.USER.getLevel() );
        RelationInfo relationInfo = relationManager.createTrustRelationship( relationInfoMeta );

        Relation relation = relationManager.buildTrustRelation( relationInfo, relationMeta );

        relationManager.saveRelation( relation );
    }


    private void destroyRelationChain( TemplateAccess templateAccess, UserDelegate from, UserDelegate to )
    {
        RelationMeta relationMeta = new RelationMeta();
        relationMeta.setSource( from );
        relationMeta.setTarget( to );
        relationMeta.setObject( templateAccess );

        Relation relation = relationManager.getRelation( relationMeta );
        if ( relation != null )
        {
            relationManager.removeRelation( relation.getId() );
        }
    }


    List<SharedTemplateInfo> getSharedTemplatesToUser( String toUserFingerprint )
    {
        UserDelegate toUserDelegate = getUserDelegate( toUserFingerprint );
        List<Relation> relationsByTarget = relationManager.getRelationsByTarget( toUserDelegate );

        List<SharedTemplateInfo> list = new ArrayList<>();
        for ( Relation relation : relationsByTarget )
        {
            if ( TEMPLATE_ACCESS_CLASS.equals( relation.getTrustedObject().getClassPath() ) )
            {
                long fromUserId = subutaiIdentityManager.getUserDelegate( relation.getSource().getUniqueIdentifier() ).getUserId();
                User fromUser = subutaiIdentityManager.getUser( fromUserId );
                String fromUserFprint = getUserFingerprint( fromUser.getSecurityKeyId() );
                if ( !toUserFingerprint.equals( fromUserFprint ) )
                {
                    list.add( new SharedTemplateInfo( fromUserFprint, toUserFingerprint,
                            relation.getTrustedObject().getUniqueIdentifier() ) );
                }
            }
        }

        return list;
    }


    List<SharedTemplateInfo> getSharedTemplateInfos( TemplateId templateId )
    {
        TemplateAccess ta = new TemplateAccess( templateId );
        List<Relation> relationsByObject = relationManager.getRelationsByObject( ta );

        List<SharedTemplateInfo> list = new ArrayList<>();
        for ( Relation relation : relationsByObject )
        {

            if ( TEMPLATE_ACCESS_CLASS.equals( relation.getTrustedObject().getClassPath() ) )
            {
                long fromUserId = subutaiIdentityManager.getUserDelegate( relation.getSource().getUniqueIdentifier() ).getUserId();
                User fromUser = subutaiIdentityManager.getUser( fromUserId );
                String fromUserFprint = getUserFingerprint( fromUser.getSecurityKeyId() );

                long toUserId = subutaiIdentityManager.getUserDelegate( relation.getTarget().getUniqueIdentifier() ).getUserId();
                User toUser = subutaiIdentityManager.getUser( toUserId );
                String toUserFprint = getUserFingerprint( toUser.getSecurityKeyId() );
                if ( !fromUserFprint.equals( toUserFprint ) )
                {
                    list.add( new SharedTemplateInfo( fromUserFprint, toUserFprint,
                            relation.getTrustedObject().getUniqueIdentifier() ) );
                }
            }
        }

        return list;
    }


    private UserDelegate getUserDelegate( String userFingerprint )
    {
        try
        {
            User toUser = subutaiIdentityManager.getUserByFingerprint( userFingerprint.toUpperCase( Locale.US ) );
            return subutaiIdentityManager.getUserDelegate( toUser.getId() );
        }
        catch ( Exception e )
        {
            throw new IllegalArgumentException( "Failed to get user with fingerprint " + userFingerprint, e );
        }
    }


    private String getUserFingerprint( String keyId )
    {
        return securityManager.getKeyManager().getKeyData( keyId ).getPublicKeyFingerprint().toLowerCase( Locale.US );
    }


    void checkGetPermission( KurjunContext context, byte[] md5, String owner ) throws AccessControlException
    {
        if ( !isGetAllowed( context, md5, owner ) )
        {
            TemplateId tid = new TemplateId( owner, Hex.encodeHexString( md5 ) );
            throw new AccessControlException(
                    String.format( "Action denied for the user %s to get a template with an id %s from the repository %s",
                            getActiveUserFingerprint(), tid.get(), context.getName() ) );
        }
    }


    void checkAddPermission( KurjunContext context ) throws AccessControlException
    {
        if ( !isAddAllowed( context ) )
        {
            throw new AccessControlException(
                    String.format( "Action denied for the user %s to add a template to the repository %s",
                            getActiveUserFingerprint(), context.getName() ) );
        }
    }


    void checkDeletePermission( KurjunContext context, byte[] md5, String owner ) throws AccessControlException
    {
        if ( !isDeleteAllowed( context, md5, owner ) )
        {
            TemplateId tid = new TemplateId( owner, Hex.encodeHexString( md5 ) );
            throw new AccessControlException(
                    String.format( "Action denied for the user %s to delete a template with an id %s from the repository %s",
                            getActiveUserFingerprint(), tid.get(), context.getName() ) );
        }
    }


    boolean isGetAllowed( KurjunContext context, byte[] md5, String owner )
    {
        switch ( context.getName() )
        {
            case TemplateRepository.PUBLIC:
                return true;

            case TemplateRepository.TRUST:
                return true;

            default:
                TemplateAccess templateAccess = new TemplateAccess( owner, Hex.encodeHexString( md5 ) );
                return relationManager.getRelationInfoManager().allHasReadPermissions( templateAccess );
        }
    }


    boolean isAddAllowed( KurjunContext context )
    {
        switch ( context.getName() )
        {
            case TemplateRepository.PUBLIC:
                return isUserHasKeyId( getActiveUserFingerprint() );

            default:
                TemplateAccess templateAccess = new TemplateAccess( context.getName(), context.getName() );
                return relationManager.getRelationInfoManager().allHasWritePermissions( templateAccess );
        }
    }


    boolean isDeleteAllowed( KurjunContext context, byte[] md5, String owner )
    {
        TemplateAccess templateAccess = new TemplateAccess( owner, Hex.encodeHexString( md5 ) );
        return relationManager.getRelationInfoManager().allHasDeletePermissions( templateAccess );
    }


    byte[] decodeMd5( String md5 )
    {
        if ( md5 != null )
        {
            try
            {
                return Hex.decodeHex( md5.toCharArray() );
            }
            catch ( DecoderException ex )
            {
                LOGGER.error( "Invalid md5 checksum", ex );
            }
        }
        return null;
    }

}
