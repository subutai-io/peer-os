package org.safehaus.subutai.common.util;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;


/**
 * @author daliev
 */
public class HttpUtil {

    public static final int RESPONSE_OK = 200;
    private static final Logger LOG = Logger.getLogger( HttpUtil.class.getName() );

    private final HttpClient client;
    private final PoolingClientConnectionManager conMan;


    public HttpUtil() {
        conMan = new PoolingClientConnectionManager( SchemeRegistryFactory.createDefault(), 3, TimeUnit.MINUTES );
        conMan.setMaxTotal( 2 );
        conMan.setDefaultMaxPerRoute( 1 );

        client = new DefaultHttpClient( conMan );

        HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout( params, 30 * 1000 );
        HttpConnectionParams.setSoTimeout( params, 60 * 1000 );
        ( ( AbstractHttpClient ) client ).setHttpRequestRetryHandler( new DefaultHttpRequestRetryHandler( 0, false ) );
    }


    public void dispose() {
        try {
            conMan.shutdown();
        }
        catch ( Exception e ) {
        }
    }


    public void cleanConnections() {
        try {
            conMan.closeExpiredConnections();
        }
        catch ( Exception e ) {
        }
    }


    public int post( String url, Map<String, String> postParams ) throws IOException {
        cleanConnections();
        HttpEntity entity = null;
        try {
            HttpPost req = new HttpPost( url );
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for ( Map.Entry<String, String> entry : postParams.entrySet() ) {
                String key = entry.getKey();
                String value = entry.getValue();
                nvps.add( new BasicNameValuePair( key, value ) );
            }
            ( ( HttpPost ) req ).setEntity( new UrlEncodedFormEntity( nvps, Consts.UTF_8 ) );
            HttpResponse httpResponse = client.execute( req );
            entity = httpResponse.getEntity();
            int resCode = httpResponse.getStatusLine().getStatusCode();

            //            LOG.warning( "ENTITY: " + EntityUtils.toString( entity, "utf-8" ).trim() );

            return resCode;
        }
        finally {
            EntityUtils.consumeQuietly( entity );
        }
    }
}
