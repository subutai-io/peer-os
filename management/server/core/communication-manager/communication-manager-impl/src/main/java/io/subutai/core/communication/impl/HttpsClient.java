package io.subutai.core.communication.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.pgp.message.PGPMessenger;
import io.subutai.core.communication.api.Response;
import io.subutai.core.communication.api.SecurityMaterials;


/**
 * HTTPS client
 */
public class HttpsClient
{
    private static final Logger log = LoggerFactory.getLogger( HttpsClient.class );


    public String post( final String uri, String data, final SecurityMaterials securityMaterials, boolean devMode )
            throws Exception
    {
        log.debug( "Sending data to: " + uri );
        HttpsPostHelper https = new HttpsPostHelper( uri, securityMaterials, devMode );

        PGPMessenger pgpMessenger = new PGPMessenger( securityMaterials.getSenderGPGPrivateKey(),
                securityMaterials.getRecipientGPGPublicKey() );

        byte[] encryptedData = pgpMessenger.produce( data.getBytes( "UTF-8" ) );

        log.debug( new String( encryptedData ) );

        Response r = https.execute( encryptedData );
        log.debug( r.toString() );

        byte[] result = pgpMessenger.consume( r.getContent().getBytes( "UTF-8" ) );
        return new String( result, "UTF-8" );
    }
}
