package io.subutai.core.kurjun.manager.impl;


import org.bouncycastle.openpgp.PGPPublicKeyRing;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Strings;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.kurjun.manager.api.KurjunManager;
import io.subutai.core.kurjun.manager.api.model.Kurjun;
import io.subutai.core.kurjun.manager.impl.dao.KurjunDataService;
import io.subutai.core.kurjun.manager.impl.model.KurjunEntity;
import io.subutai.core.kurjun.manager.impl.model.KurjunType;
import io.subutai.core.kurjun.manager.impl.utils.PropertyUtils;
import io.subutai.core.security.api.SecurityManager;


/**
 *
 */
public class KurjunManagerImpl implements KurjunManager
{

    //**********************************
    private IdentityManager identityManager = null;
    private SecurityManager securityManager = null;
    private DaoManager daoManager;
    private KurjunDataService dataService;
    //**********************************


    //TODO getValues from SystemManager;

    private String localKurjunURL;
    private String globalKurjunURL;


    //****************************************
    public void KurjunManagerImpl( IdentityManager identityManager, SecurityManager securityManager,
                                   DaoManager daoManager )
    {
        this.identityManager = identityManager;
        this.securityManager = securityManager;
        this.daoManager = daoManager;

        dataService = new KurjunDataService(daoManager);
    }


    //****************************************
    public void init()
    {
        String fingerprint = identityManager.getActiveUser().getFingerprint();

        if(Strings.isNullOrEmpty( getUser( KurjunType.Local.getId(), fingerprint ) ))
        {
            registerUser( KurjunType.Local.getId() ,fingerprint );
        }
        else
        {
            authorizeUser( KurjunType.Local.getId(),fingerprint );
        }
    }


    //****************************************
    private String getKurjunUrl(int kurjunType, String uri )
    {
        if(kurjunType == KurjunType.Local.getId())
        {
            return localKurjunURL+uri;
        }
        else if(kurjunType == KurjunType.Global.getId())
        {
            return globalKurjunURL+uri;
        }
        else
        {
            return globalKurjunURL+uri;
        }
    }


    //****************************************
    @Override
    public String registerUser( int kurjunType, String fingerprint )
    {
        //*****************************************
        String authId = "";

        if(Strings.isNullOrEmpty( getUser( kurjunType, fingerprint )))
        {
            String url = getKurjunUrl(kurjunType, PropertyUtils.getValue( "url.identity.user.add" ));
            WebClient client = RestUtil.createTrustedWebClient( url );
            //TODO get authID from client
            //authId = client Output;
        }


        if(dataService.getKurjunData( fingerprint ) == null)
        {

            //************* Sign *********************
            String signedMessage = ""; //securityManager.getEncryptionTool().
            //****************************************

            Kurjun kurjun = new KurjunEntity();
            kurjun.setType( kurjunType );
            kurjun.setOwnerFingerprint( fingerprint );
            kurjun.setAuthID( authId );
            kurjun.setSignedMessage( signedMessage );
            dataService.persistKurjunData( kurjun );
        }

        return null;
    }


    //****************************************
    @Override
    public String authorizeUser( int kurjunType, String fingerprint )
    {
        String url = getKurjunUrl(kurjunType, PropertyUtils.getValue( "url.identity.user.get" ));
        WebClient client = RestUtil.createTrustedWebClient( url );

        return null;
    }


    //****************************************
    @Override
    public boolean setSystemOwner( int kurjunType, String fingerprint )
    {

        return true;
    }


    //****************************************
    @Override
    public String getSystemOwner( int kurjunType )
    {
        String url = getKurjunUrl(kurjunType, PropertyUtils.getValue( "url.identity.user.auth" ));
        WebClient client = RestUtil.createTrustedWebClient( url );

        return null;
    }


    //****************************************
    @Override
    public String getUser( int kurjunType, String fingerprint )
    {
        String url = getKurjunUrl(kurjunType, PropertyUtils.getValue( "url.identity.user.get" ));
        WebClient client = RestUtil.createTrustedWebClient( url );


        return null;
    }

}
