package io.subutai.core.environment.impl.relation;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.RelationVerificationException;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfo;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationLink;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.security.relation.model.RelationStatus;
import io.subutai.core.environment.impl.dao.RelationDataService;
import io.subutai.core.environment.impl.entity.relation.RelationImpl;
import io.subutai.core.environment.impl.entity.relation.RelationInfoImpl;
import io.subutai.core.environment.impl.entity.relation.RelationLinkImpl;
import io.subutai.core.security.api.SecurityManager;


/**
 * Created by talas on 12/10/15.
 */
public class RelationManagerImpl implements RelationManager
{
    private static final Logger logger = LoggerFactory.getLogger( RelationManagerImpl.class );
    private static final String context = "EnvironmentManager";
    private SecurityManager securityManager;
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
        relationInfoManager = new RelationInfoManagerImpl( relationDataService, keyTrustCheckEnabled );
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
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
        RelationLinkImpl source = new RelationLinkImpl( relationMeta.getSourceId(), relationMeta.getSourcePath(),
                relationMeta.getContext() );
        RelationLinkImpl target = new RelationLinkImpl( relationMeta.getTargetId(), relationMeta.getTargetPath(),
                relationMeta.getContext() );
        RelationLinkImpl object = new RelationLinkImpl( relationMeta.getObjectId(), relationMeta.getObjectPath(),
                relationMeta.getContext() );

        //TODO try to pass interface as is
        RelationImpl relation =
                new RelationImpl( source, target, object, ( RelationInfoImpl ) relationInfo, relationMeta.getKeyId() );

        saveRelation( relation );

        return relationDataService.findBySourceTargetObject( source, target, object );
    }


    @Override
    public Relation getRelation( final RelationMeta relationMeta )
    {
        RelationLinkImpl source = new RelationLinkImpl( relationMeta.getSourceId(), relationMeta.getSourcePath(),
                relationMeta.getContext() );
        RelationLinkImpl target = new RelationLinkImpl( relationMeta.getTargetId(), relationMeta.getTargetPath(),
                relationMeta.getContext() );
        RelationLinkImpl object = new RelationLinkImpl( relationMeta.getObjectId(), relationMeta.getObjectPath(),
                relationMeta.getContext() );
        return relationDataService.findBySourceTargetObject( source, target, object );
    }


    @Override
    public void saveRelation( final Relation relation )
    {
        //TODO check if relation valid otherwise break relation build
        relationDataService.update( relation.getSource() );
        relationDataService.update( relation.getTarget() );
        relationDataService.update( relation.getTrustedObject() );
        relationDataService.update( relation );
    }


    @Override
    public RelationInfoManagerImpl getRelationInfoManager()
    {
        return relationInfoManager;
    }


    @Override
    public RelationLink getRelationLink( final String uniqueId, final String objectClass )
    {
        return relationDataService.findRelationLink( uniqueId, objectClass );
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
