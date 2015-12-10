package io.subutai.core.identity.impl;


import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.model.Relation;
import io.subutai.core.identity.api.model.RelationInfo;
import io.subutai.core.identity.api.model.RelationLink;
import io.subutai.core.identity.api.model.RelationMeta;
import io.subutai.core.identity.api.relation.TrustRelationManager;
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
    private DaoManager daoManager = null;


    //*****************************************
    public void init()
    {
        identityDataService = new IdentityDataServiceImpl( daoManager );
        trustMessageManager = new TrustMessageManagerImpl( identityManager.getSecurityManager() );
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
    public void processTrustMessage( String encrypted )
    {
        try
        {
            //TODO get verification step
            Relation relation = trustMessageManager.decryptAndVerifyMessage( encrypted );

            // TODO check if source can declare this relation
            identityDataService.persistRelation( relation );
        }
        catch ( Exception e )
        {
            logger.warn( "Error decrypting trust message.", e );
        }
    }


    // TODO Before creating trust relation decrypt verify incoming trust message
    @Override
    public void createTrustRelationship( final Map<String, String> relationshipProp )
    {
        identityDataService.createTrustRelationship( relationshipProp );
    }


    // TODO walk through nested trust relations take into account trust weight of relationship
    @Override
    public boolean isRelationValid( final String sourceId, final String sourcePath, final String objectId,
                                    final String objectPath, String statement )
    {
        RelationLink source = identityDataService.getTrustItem( sourceId, sourcePath );
        RelationLink object = identityDataService.getTrustItem( objectId, objectPath );

        Relation relation = identityDataService.getRelationBySourceObject( source, object );

        if ( relation == null )
        {
            return false;
        }

        RelationInfo relationInfo = relation.getRelationInfo();
        RelationInfo parsedRelationship = trustMessageManager.serializeMessage( statement );

        if ( !relationInfo.getContext().equalsIgnoreCase( parsedRelationship.getContext() ) )
        {
            return false;
        }

        if ( !relationInfo.getType().equalsIgnoreCase( parsedRelationship.getType() ) )
        {
            return false;
        }

        return relationInfo.getOperation().containsAll( parsedRelationship.getOperation() );
    }


    @Override
    public RelationInfo generateTrustRelationship( final String pObject, final Set<String> operation,
                                                   final String type )
    {
        return new RelationInfoImpl( pObject, operation, type );
    }


    @Override
    public Relation buildTrustRelation( final RelationInfo relationInfo, final RelationMeta relationMeta )
    {
        RelationLinkImpl source = new RelationLinkImpl( relationMeta.getSourceId(), relationMeta.getSourcePath() );
        RelationLinkImpl target = new RelationLinkImpl( relationMeta.getTargetId(), relationMeta.getTargetPath() );
        RelationLinkImpl object = new RelationLinkImpl( relationMeta.getObjectId(), relationMeta.getObjectPath() );

        //TODO try to pass interface as is
        return new RelationImpl( source, target, object, new RelationInfoImpl( relationInfo ) );
    }


    @Override
    public void executeRelationBuild( final Relation relation )
    {
        //TODO check if relation valid otherwise break relation build
        identityDataService.persistRelation( relation );
    }
}
