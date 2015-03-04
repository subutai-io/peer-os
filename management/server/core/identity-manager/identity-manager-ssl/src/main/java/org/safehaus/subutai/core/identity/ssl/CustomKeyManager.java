package org.safehaus.subutai.core.identity.ssl;


import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.X509KeyManager;

import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreData;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by talas on 3/3/15.
 */
public class CustomKeyManager implements X509KeyManager
{
    private static final Logger log = LoggerFactory.getLogger( CustomKeyManager.class );

    private String keyStorePath;

    private String keyStorePassword;

    private KeyStoreManager keyStoreManager;

    private KeyStoreData keyStoreData;


    public CustomKeyManager( final String keyStorePath, final String keyStorePassword )
    {
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        keyStoreManager = new KeyStoreManager();
        keyStoreData = new KeyStoreData();
        keyStoreData.setupKeyStorePx2();
    }


    @Override
    public String[] getClientAliases( final String keyType, final Principal[] issuers )
    {
        log.warn( String.format( "%s %s", keyType, Arrays.asList( issuers ) ) );
        log.warn( "################ getClientAliases" );
        //
        //        KeyStore keyStore = keyStoreManager.load( keyStoreData );
        //        List<String> aliases = new ArrayList<>();
        //        try
        //        {
        //            aliases = Collections.list( keyStore.aliases() );
        //        }
        //        catch ( KeyStoreException e )
        //        {
        //            log.error( "Error accessing aliases", e );
        //        }
        //        return aliases.toArray( new String[aliases.size()] );
        return getAliases( issuers );
    }


    @Override
    public String chooseClientAlias( final String[] keyType, final Principal[] issuers, final Socket socket )
    {
        String result = getAliases( issuers )[0];
        log.warn( "############ chooseClientAlias" );
        return result;
    }


    @Override
    public String[] getServerAliases( final String keyType, final Principal[] issuers )
    {
        log.warn( "############## getServerAliases" );
        return new String[0];
    }


    @Override
    public String chooseServerAlias( final String keyType, final Principal[] issuers, final Socket socket )
    {
        log.warn( "################# chooseServerAlias" );
        return null;
    }


    @Override
    public X509Certificate[] getCertificateChain( final String alias )
    {
        log.warn( "################## getCertificateChain" );
        return new X509Certificate[0];
    }


    @Override
    public PrivateKey getPrivateKey( final String alias )
    {
        log.warn( "################## getPrivateKey" );
        return null;
    }


    // make a Set out of the array
    private Set<Principal> getIssuerSet( Principal[] issuers )
    {
        if ( ( issuers != null ) && ( issuers.length != 0 ) )
        {
            return new HashSet<>( Arrays.asList( issuers ) );
        }
        else
        {
            return null;
        }
    }


    private String[] getAliases( final Principal[] issuers )
    {
        Set<Principal> issuerSet = getIssuerSet( issuers );
        KeyStore keyStore = keyStoreManager.load( keyStoreData );
        List<String> result = new ArrayList<>();
        try
        {
            for ( Enumeration<String> enumeration = keyStore.aliases(); enumeration.hasMoreElements(); )
            {
                String alias = enumeration.nextElement();
                Certificate[] chain = keyStore.getCertificateChain( alias );
                if ( issuerSet != null )
                {
                    boolean found = false;
                    for ( Certificate cert : chain )
                    {
                        X509Certificate xcert = ( X509Certificate ) cert;
                        if ( issuerSet.contains( xcert.getIssuerX500Principal() ) )
                        {
                            found = true;
                            break;
                        }
                    }
                    if ( !found )
                    {
                        log.warn( "Certificate issuer not found." );
                    }
                }
                try
                {
                    X509Certificate cert = ( X509Certificate ) chain[0];
                    cert.checkValidity( new Date() );
                    result.add( alias );
                    break;
                }
                catch ( CertificateException ignore )
                {
                }
            }
        }
        catch ( KeyStoreException e )
        {
            log.error( "Error accessing keyStore aliases.", e );
        }
        return result.toArray( new String[result.size()] );
    }
}
