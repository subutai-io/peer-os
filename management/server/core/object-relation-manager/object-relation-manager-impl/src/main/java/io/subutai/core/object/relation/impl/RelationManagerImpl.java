package io.subutai.core.object.relation.impl;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.object.relation.api.RelationManager;
import io.subutai.core.object.relation.api.RelationVerificationException;
import io.subutai.core.object.relation.api.model.Relation;
import io.subutai.core.object.relation.api.model.RelationInfo;
import io.subutai.core.object.relation.api.model.RelationInfoMeta;
import io.subutai.core.object.relation.api.model.RelationMeta;
import io.subutai.core.object.relation.api.model.RelationStatus;
import io.subutai.core.object.relation.impl.dao.RelationDataService;
import io.subutai.core.object.relation.impl.model.RelationImpl;
import io.subutai.core.object.relation.impl.model.RelationInfoImpl;
import io.subutai.core.object.relation.impl.model.RelationLinkImpl;
import io.subutai.core.security.api.SecurityManager;


/**
 * Created by talas on 12/10/15.
 */
public class RelationManagerImpl implements RelationManager
{
    private static final Logger logger = LoggerFactory.getLogger( RelationManagerImpl.class );
    private static final String context = "RelationManager";
    private SecurityManager securityManager;
    private IdentityManager identityManager;
    private RelationMessageManagerImpl trustMessageManager;
    private RelationInfoManagerImpl relationInfoManager;
    private DaoManager daoManager = null;
    private boolean keyTrustCheckEnabled;
    private RelationDataService relationDataService;


    //*****************************************
    public void init()
    {
        relationDataService = new RelationDataService( daoManager );
        trustMessageManager = new RelationMessageManagerImpl( securityManager );
        relationInfoManager = new RelationInfoManagerImpl( relationDataService, keyTrustCheckEnabled, identityManager );
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


    public boolean isKeyTrustCheckEnabled()
    {
        return keyTrustCheckEnabled;
    }


    public void setKeyTrustCheckEnabled( final boolean keyTrustCheckEnabled )
    {
        this.keyTrustCheckEnabled = keyTrustCheckEnabled;
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
                    .findBySourceTargetObject                                ( ( RelationLinkImpl ) relation
                            .getSource(),
                            ( RelationLinkImpl ) relation.getTarget(),
                            ( RelationLinkImpl ) relation.getTrustedObject() );

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
                throw new RelationVerificationException( "Relations status property differs" );
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
    public RelationInfo createTrustRelationship( final RelationInfoMeta relationInfoMeta )
    {
        return new RelationInfoImpl( relationInfoMeta );
    }


    @Override
    public Relation buildTrustRelation( final RelationInfo relationInfo, final RelationMeta relationMeta )
    {
        RelationLinkImpl source = new RelationLinkImpl( relationMeta.getSource() );
        RelationLinkImpl target = new RelationLinkImpl( relationMeta.getTarget() );
        RelationLinkImpl object = new RelationLinkImpl( relationMeta.getObject() );

        //TODO try to pass interface as is
        RelationImpl relation =
                new RelationImpl( source, target, object, ( RelationInfoImpl ) relationInfo, relationMeta.getKeyId() );

        saveRelation( relation );

        return relationDataService.findBySourceTargetObject( source, target, object );
    }


    @Override
    public Relation getRelation( final RelationMeta relationMeta )
    {
        RelationLinkImpl source = new RelationLinkImpl( relationMeta.getSource() );
        RelationLinkImpl target = new RelationLinkImpl( relationMeta.getTarget() );
        RelationLinkImpl object = new RelationLinkImpl( relationMeta.getObject() );
        return relationDataService.findBySourceTargetObject( source, target, object );
    }


    @Override
    public void saveRelation( final Relation relation )
    {
        //TODO check if relation valid otherwise break relation build
        relationDataService.save( relation.getSource() );
        relationDataService.save( relation.getTarget() );
        relationDataService.save( relation.getTrustedObject() );
        relationDataService.update( relation );
    }


    @Override
    public RelationInfoManagerImpl getRelationInfoManager()
    {
        return relationInfoManager;
    }


    @Override
    public RelationLink getRelationLink( final RelationLink relationLink )
    {
        return relationDataService.findRelationLink( relationLink );
    }


    @Override
    public List<Relation> getRelationsByObject( final RelationLink objectRelationLink )
    {
        return relationDataService.findByObject( ( RelationLinkImpl ) objectRelationLink );
    }


    @Override
    public void removeRelation( final long relationId )
    {
        relationDataService.remove( relationId );
    }
}
