package io.subutai.core.object.relation.impl;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.RelationVerificationException;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.security.relation.model.RelationStatus;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.object.relation.impl.dao.RelationDataService;
import io.subutai.core.object.relation.impl.model.RelationChallengeImpl;
import io.subutai.core.object.relation.impl.model.RelationImpl;
import io.subutai.core.object.relation.impl.model.RelationInfoImpl;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;


public class RelationManagerImpl implements RelationManager
{
    private static final Logger logger = LoggerFactory.getLogger( RelationManagerImpl.class );
    private static final String context = "RelationManager";
    private SecurityManager securityManager;
    private IdentityManager identityManager;
    private RelationMessageManagerImpl trustMessageManager;
    private RelationInfoManagerImpl relationInfoManager;
    private DaoManager daoManager = null;
    private RelationDataService relationDataService;


    //*****************************************
    public void init()
    {
        relationDataService = new RelationDataService( daoManager );
        trustMessageManager = new RelationMessageManagerImpl( securityManager );
        relationInfoManager = new RelationInfoManagerImpl( relationDataService, identityManager, securityManager );
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    @Override
    public String getContext()
    {
        return context;
    }


    @Override
    public void processTrustMessage( String signedMessage, String secretKeyId ) throws RelationVerificationException
    {
        try
        {
            Relation relation = trustMessageManager.decryptAndVerifyMessage( signedMessage, secretKeyId );

            if ( relation == null )
            {
                throw new RelationVerificationException( "Error relation declaration cannot be null." );
            }

            // Verification check have to be applied to verify that stored data is the same as the one being supported
            Relation storedRelation = relationDataService
                    .findBySourceTargetObject( relation.getSource(), relation.getTarget(),
                            relation.getTrustedObject() );

            if ( storedRelation == null )
            {
                throw new RelationVerificationException( "Error relation declaration has been deleted." );
            }

            if ( storedRelation.getId() != relation.getId() || !storedRelation.equals( relation ) )
            {
                throw new RelationVerificationException( "Relations do not match" );
            }

            if ( storedRelation.getRelationStatus() != relation.getRelationStatus() )
            {
                throw new RelationVerificationException( "Relations' status property differs" );
            }

            storedRelation.setRelationStatus( RelationStatus.VERIFIED );
            // Check for relation validity is checked before relation stating link send its request
            // TODO check if source can declare this relation
            saveRelation( relation );
        }
        catch ( Exception e )
        {
            logger.warn( "Error decrypting trust message.", e );
            throw new RelationVerificationException( e );
        }
    }


    @Override
    public Relation buildRelation( final RelationInfoMeta relationInfoMeta, final RelationMeta relationMeta )
    {
        RelationInfoImpl relationInfo = new RelationInfoImpl( relationInfoMeta );
        if ( relationMeta.getSource() == null )
        {
            User activeUser = identityManager.getActiveUser();
            UserDelegate delegatedUser = null;
            if ( activeUser != null )
            {
                delegatedUser = identityManager.getUserDelegate( activeUser.getId() );
            }
            relationMeta.setSource( delegatedUser );
        }

        RelationImpl relation =
                new RelationImpl( relationMeta.getSource(), relationMeta.getTarget(), relationMeta.getObject(),
                        relationInfo, relationMeta.getKeyId() );

        saveRelation( relation );

        return relationDataService.findBySourceTargetObject( relationMeta.getSource(), relationMeta.getTarget(),
                relationMeta.getObject() );
    }


    @Override
    public String getRelationChallenge( final long ttl ) throws RelationVerificationException
    {
        RelationChallengeImpl relationToken = new RelationChallengeImpl( ttl );
        relationDataService.save( relationToken );

        String content = JsonUtil.toJson( relationToken );
        securityManager.getKeyManager().getPublicKey( null );
        try
        {
            KeyManager keyManager = securityManager.getKeyManager();
            byte[] encBytes = PGPEncryptionUtil.encrypt( content.getBytes(), keyManager.getPublicKey( null ), true );
            return "\n" + new String( encBytes, StandardCharsets.UTF_8 );
        }
        catch ( PGPException e )
        {
            logger.error( "Error encrypting message for relation challenge", e );
            throw new RelationVerificationException( "Error encrypting message for relation challenge", e );
        }
    }


    @Override
    public Relation getRelation( final RelationMeta relationMeta )
    {
        return relationDataService.findBySourceTargetObject( relationMeta.getSource(), relationMeta.getTarget(),
                relationMeta.getObject() );
    }


    @Override
    public void saveRelation( final Relation relation )
    {
        //TODO check if relation valid otherwise break relation build

        relationDataService.updateBatch(
                Sets.<Object>newHashSet( relation.getSource(), relation.getTarget(), relation.getTrustedObject() ) );
        relationDataService.update( relation );
    }


    @Override
    public RelationInfoManagerImpl getRelationInfoManager()
    {
        return relationInfoManager;
    }


    @Override
    public List<Relation> getRelations()
    {
        return relationDataService.getAllRelations();
    }


    @Override
    public List<Relation> getRelationsByObject( final RelationLink objectRelationLink )
    {
        return relationDataService.findByObject( objectRelationLink );
    }


    @Override
    public List<Relation> getRelationsBySource( final RelationLink sourceRelationLink )
    {
        return relationDataService.findBySource( sourceRelationLink );
    }


    @Override
    public List<Relation> getRelationsByTarget( final RelationLink targetRelationLink )
    {
        return relationDataService.findByTarget( targetRelationLink );
    }


    @Override
    public void removeRelation( final long relationId )
    {
        relationDataService.remove( relationId );
    }


    @Override
    public void removeRelation( final RelationMeta relationMeta )
    {
        Relation relation = relationDataService
                .findBySourceTargetObject( relationMeta.getSource(), relationMeta.getTarget(),
                        relationMeta.getObject() );
        removeRelation( relation.getId() );
    }


    @Override
    public void removeRelation( final RelationLink link )
    {
        relationDataService.removeAllRelationsWithLink( link );
    }


    @Override
    public RelationLink getRelationLink( final String uniqueId )
    {
        return relationDataService.getRelationLinkByUniqueId( uniqueId );
    }
}
