package io.subutai.core.karaf.manager.rest;


import javax.ws.rs.core.Response;

import io.subutai.core.karaf.manager.api.KarafManager;


/**
 * Implementation of KarafManagerRest
 */
public class KarafManagerRestImpl implements KarafManagerRest
{
    private KarafManager karafManager = null;

    public KarafManagerRestImpl(KarafManager karafManager)
    {
        this.karafManager = karafManager;
    }


    @Override
    public Response runCommand( final String command )
    {
        String output = "No Result for command:"+command;

        try
        {
            output = karafManager.executeShellCommand( command );
        }
        catch(Exception ex)
        {

        }

        return Response.ok( output ).build();
    }
}
