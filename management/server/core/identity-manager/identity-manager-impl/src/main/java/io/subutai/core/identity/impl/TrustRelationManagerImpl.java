package io.subutai.core.identity.impl;


import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.model.TrustItem;
import io.subutai.core.identity.api.model.TrustRelation;
import io.subutai.core.identity.api.model.TrustRelationship;
import io.subutai.core.identity.api.relation.TrustRelationManager;
import io.subutai.core.identity.impl.dao.IdentityDataServiceImpl;


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
            TrustRelation trustRelation = trustMessageManager.decryptAndVerifyMessage( encrypted );

            // TODO check if source can declare this relation
            identityDataService.persistTrustRelation( trustRelation );
        }
        catch ( PGPException | UnsupportedEncodingException e )
        {
            logger.warn( "Error decrypting trust message." );
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
        TrustItem source = identityDataService.getTrustItem( sourceId, sourcePath );
        TrustItem object = identityDataService.getTrustItem( objectId, objectPath );

        TrustRelation trustRelation = identityDataService.getRelationBySourceObject( source, object );

        TrustRelationship parsedRelationship = trustMessageManager.serializeMessage( statement );

        return trustRelation != null && trustRelation.getRelationship().equals( parsedRelationship );
    }
}
