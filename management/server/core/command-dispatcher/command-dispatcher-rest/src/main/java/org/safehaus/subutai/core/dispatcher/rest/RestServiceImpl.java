package org.safehaus.subutai.core.dispatcher.rest;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.BatchRequest;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class RestServiceImpl implements RestService {
    private static final Logger LOG = Logger.getLogger( RestServiceImpl.class.getName() );

    private final CommandDispatcher dispatcher;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public RestServiceImpl( final CommandDispatcher dispatcher ) {
        this.dispatcher = dispatcher;
    }


    @Override
    public Response processResponses( final String responses ) {
        try {
            Set<org.safehaus.subutai.common.protocol.Response> resps = gson.fromJson( responses,
                    new TypeToken<LinkedHashSet<org.safehaus.subutai.common.protocol.Response>>() {}.getType() );
            dispatcher.processResponses( resps );
            return Response.ok().build();
        }
        catch ( RuntimeException e ) {
            LOG.severe( e.getMessage() );
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response executeRequests( final String requests ) {
        try {

            Message message = PhaseInterceptorChain.getCurrentMessage();
            //            Request request = ( Request ) message.get( AbstractHTTPDestination.HTTP_REQUEST );
            //            String ip =    request.getRemoteAddr();
            //for now set IP to local subutai
            //TODO remove this in production
            String ip = getLocalIp();
            Set<BatchRequest> reqs = gson.fromJson( requests, new TypeToken<Set<BatchRequest>>() {}.getType() );
            dispatcher.executeRequests( ip, reqs );
            return Response.ok().build();
        }
        catch ( RuntimeException e ) {
            LOG.severe( e.getMessage() );
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }


    private String getLocalIp() {
        Enumeration<NetworkInterface> n = null;
        try {
            n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements(); ) {
                NetworkInterface e = n.nextElement();

                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements(); ) {
                    InetAddress addr = a.nextElement();
                    if ( addr.getHostAddress().startsWith( "172" ) ) {
                        return addr.getHostAddress();
                    }
                }
            }
        }
        catch ( SocketException e ) {
            LOG.severe( e.getMessage() );
        }


        return "172.16.192.64";
    }
}
