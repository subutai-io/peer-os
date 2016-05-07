package io.subutai.core.object.relation.rest;


import javax.ws.rs.core.Response;

import io.subutai.common.security.relation.RelationManager;


/**
 * Created by ape-craft on 3/19/16.
 */
public class RelationManagerRestImpl implements RelationManagerRest
{
    private RelationManager relationManager;


    public RelationManagerRestImpl( final RelationManager relationManager )
    {
        this.relationManager = relationManager;
    }


    @Override
    public Response getRelationChallenge()
    {
        try
        {
            return Response.ok( relationManager.getRelationChallenge( -1 ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() )
                           .build();
        }
    }
}
