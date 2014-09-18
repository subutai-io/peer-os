package org.safehaus.subutai.core.dispatcher.rest;


import java.util.logging.Logger;

import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class RestServiceImpl implements RestService {
    private static final Logger LOG = Logger.getLogger( RestServiceImpl.class.getName() );

    private final CommandDispatcher dispatcher;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public RestServiceImpl( final CommandDispatcher dispatcher ) {
        this.dispatcher = dispatcher;
    }
}
