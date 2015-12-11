package io.subutai.core.identity.impl;


import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.objects.PermissionOperation;
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

        if ( relationInfo.getOwnershipLevel() < parsedRelationship.getOwnershipLevel() )
        {
            return false;
        }

        return relationInfo.getOperation().containsAll( parsedRelationship.getOperation() );
    }


    @Override
    public boolean isRelationValid( final RelationInfo relationInfo, final RelationMeta relationMeta )
    {
        RelationLinkImpl target = new RelationLinkImpl( relationMeta.getSourceId(), relationMeta.getSourcePath() );
        List<Relation> targetRelations = identityDataService.relationsByTarget( target );

        RelationLinkImpl object = new RelationLinkImpl( relationMeta.getObjectId(), relationMeta.getObjectPath() );
        List<Relation> objectRelations = identityDataService.relationsByTarget( object );

        // When relation info is found check that relation was granted from verified source
        for ( final Relation targetRelation : targetRelations )
        {
            if ( targetRelation.getTrustedObject().equals( object ) )
            {
                // Requested relation should be less then or equal to relation that was granted
                return compareRelationships( targetRelation.getRelationInfo(), relationInfo ) >= 0;
            }
        }

        return false;
    }


    /**
     * Compare relationship weight depending on each relationship property, if relation context or level differs then
     * this relation is not comparable
     */
    private int compareRelationships( RelationInfo a, RelationInfo b )
    {
        if ( !a.getContext().equalsIgnoreCase( b.getContext() ) )
        {
            return -2;
        }

        int ownership = 0;
        if ( a.getOwnershipLevel() > b.getOwnershipLevel() )
        {
            ownership = 1;
        }
        else if ( a.getOwnershipLevel() < b.getOwnershipLevel() )
        {
            ownership = -1;
        }

        //Calculate permission operations level
        int operation = 0;
        int operationA = sumUpOperationLevel( a.getOperation() );
        int operationB = sumUpOperationLevel( b.getOperation() );

        if ( operationA > operationB )
        {
            operation = 1;
        }
        else if ( operationA < operationB )
        {
            operation = -1;
        }

        if ( operation == 0 )
        {
            return ownership;
        }
        return operation;
    }


    private int sumUpOperationLevel( Set<String> operations )
    {
        int sum = 0;
        for ( final String operation : operations )
        {
            sum += PermissionOperation.getByName( operation ).getId();
        }
        return sum;
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
        return new RelationImpl( source, target, object, new RelationInfoImpl( relationInfo ) );
    }


    @Override
    public void executeRelationBuild( final Relation relation )
    {
        //TODO check if relation valid otherwise break relation build
        identityDataService.persistRelation( relation );
    }
}
