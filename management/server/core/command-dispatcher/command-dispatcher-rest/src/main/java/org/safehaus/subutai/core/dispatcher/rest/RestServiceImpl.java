package org.safehaus.subutai.core.dispatcher.rest;


import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;


public class RestServiceImpl implements RestService
{

    private final CommandDispatcher dispatcher;


    public RestServiceImpl( final CommandDispatcher dispatcher )
    {
        this.dispatcher = dispatcher;
    }
}
