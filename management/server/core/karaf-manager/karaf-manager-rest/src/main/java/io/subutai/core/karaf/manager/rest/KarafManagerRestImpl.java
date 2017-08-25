package io.subutai.core.karaf.manager.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.karaf.manager.api.KarafManager;


/**
 * Implementation of KarafManagerRest
 */
public class KarafManagerRestImpl implements KarafManagerRest
{
    private static final Logger LOG = LoggerFactory.getLogger( KarafManagerRestImpl.class.getName() );

    private KarafManager karafManager = null;


    public KarafManagerRestImpl( KarafManager karafManager )
    {
        this.karafManager = karafManager;
    }


    @Override
    public Response runCommand( final String command )
    {
        String output = "No Result for command:" + command;

        try
        {
            output = karafManager.executeShellCommand( command );
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok( output ).build();
    }
}
