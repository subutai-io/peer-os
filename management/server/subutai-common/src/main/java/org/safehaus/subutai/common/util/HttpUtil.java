package org.safehaus.subutai.common.util;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.safehaus.subutai.common.exception.HTTPException;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


/**
 * @author daliev
 */
public class HttpUtil
{

    public static final int RESPONSE_OK = 200;
    private static final Logger LOG = Logger.getLogger( HttpUtil.class.getName() );


    public static String request( RequestType requestType, String url, Map<String, String> params ) throws HTTPException
    {
        if ( requestType == RequestType.GET )
        {
            return get( url, params );
        }
        else
        {
            return post( url, params );
        }
    }


    private static String get( String url, Map<String, String> getParamss ) throws HTTPException
    {

        HttpEntity entity = null;
        CloseableHttpResponse response = null;
        try
        {
            URIBuilder builder = new URIBuilder();
            String destURL = url.replace( "http://", "" ).replace( "https://", "" );
            String[] hostPortnParams = destURL.split( "\\?" );
            int port = 80;
            String host, path;
            String[] hostPortParts = hostPortnParams[0].split( ":" );
            if ( hostPortParts.length == 1 )
            {
                String[] arr = hostPortParts[0].split( "/", 2 );
                host = arr[0];

                path = "/";
                if ( arr.length > 1 )
                {
                    path += arr[1];
                }
            }
            else
            {
                host = hostPortParts[0];
                String[] arr = hostPortParts[1].split( "/", 2 );
                port = Integer.valueOf( arr[0] );
                path = "/";
                if ( arr.length > 1 )
                {
                    path += arr[1];
                }
            }

            Map<String, String> getParams = new HashMap<String, String>();
            if ( getParamss != null )
            {
                getParams.putAll( getParamss );
            }


            if ( hostPortnParams.length > 1 )
            {
                String[] getParamPairs = hostPortnParams[1].split( "&" );
                for ( String getParamPair : getParamPairs )
                {
                    String[] pair = getParamPair.split( "=" );
                    if ( pair.length == 2 )
                    {
                        String name = pair[0].trim();
                        String value = pair[1].trim();
                        getParams.put( name, value );
                    }
                }
            }
            builder.setScheme( "http" ).setHost( host ).setPort( port ).setPath( path );
            for ( Map.Entry<String, String> entry : getParams.entrySet() )
            {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.setParameter( key, value );
            }
            URI uri = builder.build();
            HttpRequestBase req = new HttpGet( uri );

            CloseableHttpClient httpclient = HttpClients.createDefault();

            response = httpclient.execute( req );
            entity = response.getEntity();
            int resCode = response.getStatusLine().getStatusCode();
            if ( resCode == RESPONSE_OK )
            {
                return EntityUtils.toString( entity, Consts.UTF_8 ).trim();
            }
            else
            {
                throw new HTTPException( String.format( "Http status code: %d", resCode ) );
            }
        }
        catch ( IOException | URISyntaxException e )
        {
            LOG.severe( String.format( "Error in get: %s", e.getMessage() ) );
            throw new HTTPException( e.getMessage() );
        }
        finally
        {
            try
            {
                EntityUtils.consumeQuietly( entity );
            }
            catch ( Exception ignore )
            {
            }

            try
            {
                if ( response != null )
                {
                    response.close();
                }
            }
            catch ( Exception ignore )
            {
            }
        }
    }


    private static String post( String url, Map<String, String> postParams ) throws HTTPException
    {
        HttpEntity entity = null;
        CloseableHttpResponse response = null;
        try
        {
            HttpPost req = new HttpPost( url );
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for ( Map.Entry<String, String> entry : postParams.entrySet() )
            {
                String key = entry.getKey();
                String value = entry.getValue();
                nvps.add( new BasicNameValuePair( key, value ) );
            }
            ( ( HttpPost ) req ).setEntity( new UrlEncodedFormEntity( nvps, Consts.UTF_8 ) );
            CloseableHttpClient httpclient = HttpClients.createDefault();
            response = httpclient.execute( req );
            entity = response.getEntity();
            int resCode = response.getStatusLine().getStatusCode();
            if ( resCode == RESPONSE_OK )
            {
                return EntityUtils.toString( entity, Consts.UTF_8 ).trim();
            }
            else
            {
                throw new HTTPException( String.format( "Http status code: %d", resCode ) );
            }
        }
        catch ( IOException e )
        {
            LOG.severe( String.format( "Error in post: %s", e.getMessage() ) );
            throw new HTTPException( e.getMessage() );
        }
        finally
        {
            try
            {
                EntityUtils.consumeQuietly( entity );
            }
            catch ( Exception ignore )
            {
            }

            try
            {
                if ( response != null )
                {
                    response.close();
                }
            }
            catch ( Exception ignore )
            {
            }
        }
    }


    public static enum RequestType
    {
        GET, POST
    }
}
