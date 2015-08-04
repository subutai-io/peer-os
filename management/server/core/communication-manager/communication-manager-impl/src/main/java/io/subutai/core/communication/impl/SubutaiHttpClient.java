package io.subutai.core.communication.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.pgp.message.PGPMessenger;
import io.subutai.core.communication.api.Response;
import io.subutai.core.communication.api.SecurityMaterials;


/**
 * Created by tzhamakeev on 8/3/15.
 */
public class SubutaiHttpClient
{
    private static final Logger log = LoggerFactory.getLogger( SubutaiHttpClient.class );
    private String uri;
    private boolean devMode = false;
    private SecurityMaterials securityMaterials;


    public SubutaiHttpClient( final String uri, final SecurityMaterials securityMaterials, boolean devMode )
    {
        this.uri = uri;
        this.devMode = devMode;
        this.securityMaterials = securityMaterials;
    }


    public String run( String data ) throws Exception
    {
        HttpsPostHelper https = new HttpsPostHelper( uri, securityMaterials, devMode );


        PGPMessenger pgpMessenger = new PGPMessenger( securityMaterials.getSenderGPGPrivateKey(),
                securityMaterials.getRecipientGPGPublicKey() );

        String encryptedData = pgpMessenger.produce( data );

        log.debug( encryptedData );
        Response r = https.execute( encryptedData );

        log.debug( r.getContent() );

        return pgpMessenger.consume( r.getContent() );
    }
}
