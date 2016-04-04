package io.subutai.core.kurjun.manager.impl;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.util.RestUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.kurjun.manager.api.KurjunManager;
import io.subutai.core.kurjun.manager.api.dao.KurjunDataService;
import io.subutai.core.kurjun.manager.api.model.Kurjun;
import io.subutai.core.kurjun.manager.impl.dao.KurjunDataServiceImpl;
import io.subutai.core.kurjun.manager.impl.model.KurjunEntity;
import io.subutai.core.kurjun.manager.impl.model.KurjunType;
import io.subutai.core.security.api.SecurityManager;


/**
 *
 */
public class KurjunManagerImpl implements KurjunManager
{
    private static final Logger LOG = LoggerFactory.getLogger( KurjunManagerImpl.class.getName() );

    //**********************************
    private IdentityManager identityManager;
    private SecurityManager securityManager;
    private DaoManager daoManager;

    private KurjunDataService dataService;
    private static Properties properties;
    private static String ownerKey;
    private static String fingerprint;
    //**********************************


    //****************************************
    public void init()
    {
        dataService = new KurjunDataServiceImpl( daoManager );

        if ( dataService.getAllKurjunData().isEmpty() )
        {
            try
            {

                for ( final String url : SystemSettings.getGlobalKurjunUrls() )
                {
                    Kurjun kurjun = new KurjunEntity();

                    kurjun.setType( KurjunType.Global.getId() );
                    kurjun.setState( false );
                    kurjun.setUrl( url );

                    dataService.persistKurjunData( kurjun );
                }

                for ( final String url : SystemSettings.getLocalKurjunUrls() )
                {
                    Kurjun kurjun = new KurjunEntity();

                    kurjun.setType( KurjunType.Local.getId() );
                    kurjun.setState( false );
                    kurjun.setUrl( url );

                    dataService.persistKurjunData( kurjun );
                }
            }
            catch ( ConfigurationException e )
            {
                e.printStackTrace();
            }
        }

        properties = loadProperties();
    }


    //****************************************
    private String getKurjunUrl( String url, String uri, final int kurjunType )
    {
        if ( kurjunType == KurjunType.Local.getId() )
        {
            return url + properties.getProperty( "local." + uri );
        }
        else if ( kurjunType == KurjunType.Global.getId() )
        {
            return url + properties.getProperty( "global." + uri );
        }

        return null;
    }


    @Override
    public String registerUser( final int id )
    {
        Kurjun kurjun = dataService.getKurjunData( id );
        PGPPublicKey key =
                securityManager.getKeyManager().getPublicKeyRing( securityManager.getKeyManager().getPeerOwnerId() )
                               .getPublicKey();
        String ownerKey = null;
        try
        {
            ownerKey = PGPKeyUtil.exportAscii( key );
        }
        catch ( PGPException e )
        {
            e.printStackTrace();
        }


        String authId = "";
        String path = getKurjunUrl( kurjun.getUrl(), "url.identity.user.add", kurjun.getType() );

        WebClient client = RestUtil.createTrustedWebClient( path );
        client.query( "key", ownerKey );

        Response response = client.post( null );

        if ( response.getStatus() == HttpStatus.SC_OK )
        {
            authId = response.readEntity( String.class );
        }
        else
        {
            return null;
        }

        kurjun.setAuthID( authId );
        kurjun.setOwnerFingerprint( PGPKeyUtil.getFingerprint( key.getFingerprint() ) );
        kurjun.setState( true );

        dataService.updateKurjunData( kurjun );

        return authId;
    }


    @Override
    public String authorizeUser( final int id, final String signedMessage )
    {
        Kurjun kurjun = getDataService().getKurjunData( id );

        String path = getKurjunUrl( kurjun.getUrl(), "url.identity.user.auth", kurjun.getType() );

        WebClient client = RestUtil.createTrustedWebClient( path );

        client.query( "fingerprint", kurjun.getOwnerFingerprint().toLowerCase() );
        client.query( "message", signedMessage );

        Response response = client.post( null );

        if ( response.getStatus() != HttpStatus.SC_OK )
        {
            return null;
        }
        else
        {
            byte[] signedMsg = Base64.encodeBase64( signedMessage.getBytes() );
            kurjun.setSignedMessage( signedMsg );
            kurjun.setToken( response.readEntity( String.class ) );
            kurjun.setState( true );

            dataService.updateKurjunData( kurjun );
            return "success";
        }
    }


    @Override
    public boolean setSystemOwner( final String url, final int kurjunType )
    {
        return false;
    }


    //****************************************
    @Override
    public String getSystemOwner( int kurjunType )
    {
        //        String url = getKurjunUrl( kurjunType, properties.getProperty( "url.identity.user.auth" ) );
        //        WebClient client = RestUtil.createTrustedWebClient( url );

        return null;
    }


    @Override
    public String getUser( final int id )
    {
        Kurjun kurjun = dataService.getKurjunData( id );
        PGPPublicKey key =
                securityManager.getKeyManager().getPublicKeyRing( securityManager.getKeyManager().getPeerOwnerId() )
                               .getPublicKey();

        String fingerprint = PGPKeyUtil.getFingerprint( key.getFingerprint() );

        String path = getKurjunUrl( kurjun.getUrl(), "url.identity.user.get", kurjun.getType() );
        WebClient client = RestUtil.createTrustedWebClient( path );
        client.query( "fingerprint", fingerprint );

        Response response = client.get();

        if ( response.getStatus() != HttpStatus.SC_OK )
        {
            LOG.error( "Could not get AuthId:" + response.readEntity( String.class ) );
            return null;
        }
        else
        {
            return "success";
        }
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    private Properties loadProperties()
    {
        Properties configProp = new Properties();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream( "rest.properties" );
        try
        {
            configProp.load( in );
            return configProp;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return null;
        }
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public KurjunDataService getDataService()
    {
        return dataService;
    }


    @Override
    public void saveUrl( final String url, final int type ) throws ConfigurationException
    {
        validateUrl( url );
        Kurjun kurjun = new KurjunEntity();
        kurjun.setUrl( url );
        kurjun.setType( type );

        dataService.persistKurjunData( kurjun );
        updateSystemSettings();
    }


    @Override
    public void updateUrl( final int id, final String url ) throws ConfigurationException
    {
        validateUrl( url );

        Kurjun kurjun = dataService.getKurjunData( id );
        kurjun.setUrl( url );

        dataService.updateKurjunData( kurjun );
        updateSystemSettings();
    }


    private void updateSystemSettings() throws ConfigurationException
    {
        ArrayList<String> globalUrls = Lists.newArrayList();
        ArrayList<String> localUrls = Lists.newArrayList();
        for ( final Kurjun entity : dataService.getAllKurjunData() )
        {
            if ( entity.getType() == 1 )
            {
                localUrls.add( entity.getUrl() );
            }
            else if ( entity.getType() == 2 )
            {
                globalUrls.add( entity.getUrl() );
            }
        }

        String[] localArr = new String[localUrls.size()];
        localArr = localUrls.toArray( localArr );

        String[] globalArr = new String[globalUrls.size()];
        globalArr = globalUrls.toArray( globalArr );

        SystemSettings.setLocalKurjunUrls( localArr );
        SystemSettings.setGlobalKurjunUrls( globalArr );
    }


    private void validateUrl( final String url ) throws ConfigurationException
    {
        try
        {
            new URL( url );
        }
        catch ( MalformedURLException e )
        {
            throw new ConfigurationException( "Invalid URL: " + url );
        }
    }
}
