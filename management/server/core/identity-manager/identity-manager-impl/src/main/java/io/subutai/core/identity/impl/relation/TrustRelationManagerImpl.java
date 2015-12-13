package io.subutai.core.identity.impl.relation;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.exception.RelationVerificationException;
import io.subutai.core.identity.api.model.Relation;
import io.subutai.core.identity.api.model.RelationInfo;
import io.subutai.core.identity.api.model.RelationMeta;
import io.subutai.core.identity.api.relation.RelationStatus;
import io.subutai.core.identity.api.relation.TrustRelationManager;
import io.subutai.core.identity.impl.IdentityManagerImpl;
import io.subutai.core.identity.impl.dao.IdentityDataServiceImpl;
import io.subutai.core.identity.impl.model.RelationImpl;
import io.subutai.core.identity.impl.model.RelationInfoImpl;
import io.subutai.core.identity.impl.model.RelationLinkImpl;


/**
 * Created by talas on 12/10/15.
 */
public class TrustRelationManagerImpl implements TrustRelationManager
{
    private static final Logger logger = LoggerFactory.getLogger( TrustRelationManagerImpl.class );
    private IdentityManagerImpl identityManager;
    private TrustMessageManagerImpl trustMessageManager;
    private IdentityDataService identityDataService = null;
    private RelationInfoManagerImpl relationInfoManager;
    private DaoManager daoManager = null;


    //*****************************************
    public void init()
    {
        identityDataService = new IdentityDataServiceImpl( daoManager );
        trustMessageManager = new TrustMessageManagerImpl( identityManager.getSecurityManager() );
        relationInfoManager = new RelationInfoManagerImpl( identityDataService );
    }


    @Override
    public RelationInfoManagerImpl getRelationInfoManager()
    {
        return relationInfoManager;
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void setIdentityManager( final IdentityManagerImpl identityManager )
    {
        this.identityManager = identityManager;
    }


    @Override
    public void processTrustMessage( String encrypted, String secretKeyId ) throws RelationVerificationException
    {
        try
        {
            Relation relation = trustMessageManager.decryptAndVerifyMessage( encrypted, secretKeyId );

            if ( relation == null )
            {
                throw new RelationVerificationException( "Error relation declaration cannot be null." );
            }

            // Verification check have to be applied to verify that stored data is the same as the one being supported
            Relation storedRelation = identityDataService
                    .getRelationBySourceTargetObject( relation.getSource(), relation.getTarget(),
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
                throw new RelationVerificationException( "Relations status property differs" );
            }

            storedRelation.setRelationStatus( RelationStatus.VERIFIED );
            // Check for relation validity is checked before relation stating link send its request
            // TODO check if source can declare this relation
            // identityDataService.persistRelation( relation );
        }
        catch ( Exception e )
        {
            logger.warn( "Error decrypting trust message.", e );
            throw new RelationVerificationException( e );
        }
    }


    @Override
    public RelationInfo generateTrustRelationship( final String pObject, final Set<String> operation,
                                                   final int ownershipLevel )
    {
        return new RelationInfoImpl( pObject, operation, ownershipLevel );
    }


    @Override
    public Relation buildTrustRelation( final RelationInfo relationInfo, final RelationMeta relationMeta )
    {
        RelationLinkImpl source = new RelationLinkImpl( relationMeta.getSourceId(), relationMeta.getSourcePath() );
        RelationLinkImpl target = new RelationLinkImpl( relationMeta.getTargetId(), relationMeta.getTargetPath() );
        RelationLinkImpl object = new RelationLinkImpl( relationMeta.getObjectId(), relationMeta.getObjectPath() );

        //TODO try to pass interface as is
        RelationImpl relation = new RelationImpl( source, target, object, new RelationInfoImpl( relationInfo ),
                relationMeta.getKeyId() );

        identityDataService.persistRelation( relation );

        return identityDataService.getRelationBySourceTargetObject( source, target, object );
    }


    @Override
    public Relation getRelation( final RelationMeta relationMeta )
    {
        RelationLinkImpl source = new RelationLinkImpl( relationMeta.getSourceId(), relationMeta.getSourcePath() );
        RelationLinkImpl target = new RelationLinkImpl( relationMeta.getTargetId(), relationMeta.getTargetPath() );
        RelationLinkImpl object = new RelationLinkImpl( relationMeta.getObjectId(), relationMeta.getObjectPath() );
        return identityDataService.getRelationBySourceTargetObject( source, target, object );
    }


    @Override
    public void executeRelationBuild( final Relation relation )
    {
        //TODO check if relation valid otherwise break relation build
        identityDataService.persistRelation( relation );
    }
}
