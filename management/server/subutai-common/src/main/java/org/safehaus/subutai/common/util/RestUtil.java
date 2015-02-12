package org.safehaus.subutai.common.util;


import java.util.Map;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.exception.HTTPException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class RestUtil
{
    private long defaultReceiveTimeout = 1000 * 60 * 5;
    private long defaultConnectionTimeout = 1000 * 60;


    public static enum RequestType
    {
        GET, POST
    }


    public RestUtil()
    {
    }


    public RestUtil( final long defaultReceiveTimeout, final long defaultConnectionTimeout )
    {
        Preconditions.checkArgument( defaultReceiveTimeout > 0, "Receive timeout must be greater than 0" );
        Preconditions.checkArgument( defaultConnectionTimeout > 0, "Connection timeout must be greater than 0" );

        this.defaultReceiveTimeout = defaultReceiveTimeout;
        this.defaultConnectionTimeout = defaultConnectionTimeout;
    }


    public String request( RequestType requestType, String url, Map<String, String> params,
                           Map<String, String> headers ) throws HTTPException
    {

        Preconditions.checkNotNull( requestType, "Invalid request type" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( url ), "Invalid url" );

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
                    if ( requestType == RequestType.GET )
                    {
                        client.query( entry.getKey(), entry.getValue() );
                    }
                    else
                    {
                        form.set( entry.getKey(), entry.getValue() );
                    }
                }
            }
            if ( headers != null )
            {
                for ( Map.Entry<String, String> entry : headers.entrySet() )
                {
                    client.header( entry.getKey(), entry.getValue() );
                }
            }
            response = requestType == RequestType.GET ? client.get() : client.form( form );
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


    protected WebClient createWebClient( String url )
    {
        WebClient client = WebClient.create( url );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( defaultConnectionTimeout );
        httpClientPolicy.setReceiveTimeout( defaultReceiveTimeout );

        httpConduit.setClient( httpClientPolicy );
        return client;
    }
}
