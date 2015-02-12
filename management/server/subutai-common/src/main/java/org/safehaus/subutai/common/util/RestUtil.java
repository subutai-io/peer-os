package org.safehaus.subutai.common.util;


import java.util.Map;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.exception.HTTPException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;


public class RestUtil
{
    private static final long DEFAULT_RECEIVE_TIMEOUT = 1000 * 60 * 5;
    private static final long DEFAULT_CONNECTION_TIMEOUT = 1000 * 60;


    public static enum RequestType
    {
        GET, POST
    }


    public String request( RequestType requestType, String url, Map<String, String> params,
                           Map<String, String> headers ) throws HTTPException
    {
        if ( requestType == RequestType.GET )
        {
            return get( url, params, headers );
        }
        else
        {
            return post( url, params, headers );
        }
    }


    public static String get( String url, Map<String, String> params, Map<String, String> headers ) throws HTTPException
    {
        WebClient client = null;
        Response response = null;
        try
        {
            client = createWebClient( url );
            if ( params != null )
            {
                for ( Map.Entry<String, String> entry : params.entrySet() )
                {
                    client.query( entry.getKey(), entry.getValue() );
                }
            }
            if ( headers != null )
            {
                for ( Map.Entry<String, String> entry : headers.entrySet() )
                {
                    client.header( entry.getKey(), entry.getValue() );
                }
            }
            response = client.get();
            if ( !NumUtil.isIntBetween( response.getStatus(), 200, 299 ) )
            {
                if ( response.hasEntity() )
                {
                    throw new HTTPException( response.readEntity( String.class ) );
                }
                else
                {
                    throw new HTTPException( String.format( "Http status code: %d", response.getStatus() ) );
                }
            }
            else if ( response.hasEntity() )
            {
                return response.readEntity( String.class );
            }
        }
        finally
        {
            if ( response != null )
            {
                try
                {
                    response.close();
                }
                catch ( Exception ignore )
                {
                }
            }
            if ( client != null )
            {
                try
                {
                    client.close();
                }
                catch ( Exception ignore )
                {
                }
            }
        }

        return null;
    }


    public static String post( String url, Map<String, String> params, Map<String, String> headers )
            throws HTTPException
    {
        WebClient client = null;
        Response response = null;
        try
        {
            client = createWebClient( url );
            Form form = new Form();
            if ( params != null )
            {
                for ( Map.Entry<String, String> entry : params.entrySet() )
                {
                    form.set( entry.getKey(), entry.getValue() );
                }
            }
            if ( headers != null )
            {
                for ( Map.Entry<String, String> entry : headers.entrySet() )
                {
                    client.header( entry.getKey(), entry.getValue() );
                }
            }
            response = client.form( form );
            if ( !NumUtil.isIntBetween( response.getStatus(), 200, 299 ) )
            {
                if ( response.hasEntity() )
                {
                    throw new HTTPException( response.readEntity( String.class ) );
                }
                else
                {
                    throw new HTTPException( String.format( "Http status code: %d", response.getStatus() ) );
                }
            }
            else if ( response.hasEntity() )
            {
                return response.readEntity( String.class );
            }
        }
        finally
        {
            if ( response != null )
            {
                try
                {
                    response.close();
                }
                catch ( Exception ignore )
                {
                }
            }
            if ( client != null )
            {
                try
                {
                    client.close();
                }
                catch ( Exception ignore )
                {
                }
            }
        }

        return null;
    }


    protected static WebClient createWebClient( String url )
    {
        WebClient client = WebClient.create( url );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( DEFAULT_CONNECTION_TIMEOUT );
        httpClientPolicy.setReceiveTimeout( DEFAULT_RECEIVE_TIMEOUT );

        httpConduit.setClient( httpClientPolicy );
        return client;
    }
}
