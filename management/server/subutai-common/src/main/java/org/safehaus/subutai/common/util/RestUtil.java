package org.safehaus.subutai.common.util;


import java.util.Map;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.exception.HTTPException;

import org.apache.cxf.jaxrs.client.WebClient;


public class RestUtil {
    public static final int RESPONSE_OK = 200;


    public static String get( String url, Map<String, String> params ) throws HTTPException {
        WebClient client = null;
        Response response = null;
        try {
            client = WebClient.create( url );
            if ( params != null ) {
                for ( Map.Entry<String, String> entry : params.entrySet() ) {
                    client.query( entry.getKey(), entry.getValue() );
                }
            }
            response = client.get();
            if ( response.getStatus() != RESPONSE_OK ) {
                throw new HTTPException( String.format( "Http status code: %d", response.getStatus() ) );
            }
            else {
                return response.readEntity( String.class );
            }
        }
        finally {
            if ( response != null ) {
                try {
                    response.close();
                }
                catch ( Exception ignore ) {
                }
            }
            if ( client != null ) {
                try {
                    client.close();
                }
                catch ( Exception ignore ) {
                }
            }
        }
    }
}
