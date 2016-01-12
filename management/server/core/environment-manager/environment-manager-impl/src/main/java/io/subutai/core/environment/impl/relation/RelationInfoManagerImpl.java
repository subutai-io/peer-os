package io.subutai.core.environment.impl.relation;


import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.relation.RelationInfoManager;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfo;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationLink;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.core.environment.impl.dao.RelationDataService;
import io.subutai.core.environment.impl.entity.relation.RelationInfoImpl;
import io.subutai.core.environment.impl.entity.relation.RelationLinkImpl;


/**
 * Created by talas on 12/11/15.
 */
public class RelationInfoManagerImpl implements RelationInfoManager
{
    private static final Logger logger = LoggerFactory.getLogger( RelationInfoManagerImpl.class );
    private boolean keyTrustCheckEnabled;
    private RelationDataService relationDataService;


    public RelationInfoManagerImpl( final RelationDataService relationDataService, final boolean keyTrustCheckEnabled )
    {
        this.relationDataService = relationDataService;
        this.keyTrustCheckEnabled = keyTrustCheckEnabled;
    }


    // TODO also verify source of trust, like if A -> B -> C, check does really A has permission to create relation
    // between B and C
    // C should give correct verification through relationship path.
    private boolean isRelationValid( final RelationInfo relationInfo, final RelationMeta relationMeta )
    {
        if ( !keyTrustCheckEnabled )
        {
            return false;
        }

        Set<RelationLink> relationLinks = Sets.newHashSet();

        RelationLinkImpl target = new RelationLinkImpl( relationMeta.getSourceId(), relationMeta.getSourcePath(),
                relationMeta.getContext() );
        List<Relation> byTargetRelations = relationDataService.findByTarget( target );

        RelationLinkImpl object = new RelationLinkImpl( relationMeta.getObjectId(), relationMeta.getObjectPath(),
                relationMeta.getContext() );
        List<Relation> bySourceRelations = relationDataService.findBySource( target );

        // When relation info is found check that relation was granted from verified source
        for ( final Relation targetRelation : byTargetRelations )
        {
            if ( targetRelation.getTrustedObject().equals( object ) )
            {
                // Requested relation should be less then or equal to relation that was granted
                return compareRelationships( targetRelation.getRelationInfo(), relationInfo ) >= 0;
            }
            int result = getDeeper( relationInfo, ( RelationLinkImpl ) targetRelation.getTrustedObject(), object,
                    relationLinks );
            if ( result != -3 )
            {
                return result >= 0;
            }
        }

        // TODO instead of getting deep one/two steps later implement full relation lookup with source - target -
        // object relationship verification
        // relationship verification should be done at transaction point between relation links, checks should be
        // applied towards granting link, as does this granting link has permissions to set new relation link and new
        // relation link doesn't exceed relation link grantee has
        for ( final Relation sourceRelation : bySourceRelations )
        {
            if ( sourceRelation.getTrustedObject().equals( object ) )
            {
                // Requested relation should be less then or equal to relation that was granted
                return compareRelationships( sourceRelation.getRelationInfo(), relationInfo ) >= 0;
            }
        }

        return false;
    }


    // return -3 means no relation exist
    private int getDeeper( final RelationInfo relationInfo, final RelationLinkImpl target, final RelationLink object,
                           Set<RelationLink> relationLinks )
    {
        if ( !keyTrustCheckEnabled )
        {
            return 0;
        }
        List<Relation> byTargetRelations = relationDataService.findByTarget( target );
        relationLinks.add( target );
        // When relation info is found check that relation was granted from verified source
        for ( final Relation targetRelation : byTargetRelations )
        {
            int compare = compareRelationships( targetRelation.getRelationInfo(), relationInfo );
            if ( targetRelation.getTrustedObject().equals( object ) )
            {
                return compare;
            }
            if ( compare >= 0 && !relationLinks.contains( targetRelation.getTrustedObject() ) )
            {
                int result = getDeeper( relationInfo, ( RelationLinkImpl ) targetRelation.getTrustedObject(), object,
                        relationLinks );
                if ( result != -3 )
                {
                    return result;
                }
            }
        }

        return -3;
    }


    /**
     * Compare relationship depending on each relationship property, if relation ownership level differs then this
     * relation is not comparable, the rest properties simply should match, and data format should come in key=value
     * format
     *  1 - a is greater
     *  0 - equal
     * -1 - a is less than
     * -2 - incomparable
     */
    private int compareRelationships( RelationInfo a, RelationInfo b )
    {
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
        if ( !a.isDeletePermission() && b.isDeletePermission() || !a.isReadPermission() && b.isReadPermission()
                || !a.isUpdatePermission() && b.isUpdatePermission() || !a.isWritePermission() && b
                .isWritePermission() )
        {
            operation = -1;
        }
        else
        {
            operation = 0;
        }

        if ( operation == 0 )
        {
            return ownership;
        }
        return operation;
    }


    @Override
    public boolean ownerHasReadPermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( true, false, false, false, Ownership.USER.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean ownerHasWritePermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( false, true, false, false, Ownership.USER.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean ownerHasDeletePermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( false, false, false, true, Ownership.USER.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean ownerHasUpdatePermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( false, false, true, false, Ownership.USER.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean groupHasReadPermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( true, false, false, false, Ownership.GROUP.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean groupHasWritePermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( false, true, false, false, Ownership.GROUP.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean groupHasDeletePermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( false, false, false, true, Ownership.GROUP.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean groupHasUpdatePermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( false, false, true, false, Ownership.GROUP.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean allHasReadPermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta = new RelationInfoMeta( true, false, false, false, Ownership.ALL.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean allHasWritePermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta = new RelationInfoMeta( false, true, false, false, Ownership.ALL.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean allHasDeletePermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta = new RelationInfoMeta( false, false, false, true, Ownership.ALL.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean allHasUpdatePermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta = new RelationInfoMeta( false, false, true, false, Ownership.ALL.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }
}
