package io.subutai.core.kurjun.manager.impl;


import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.kurjun.manager.api.KurjunManager;
import io.subutai.core.kurjun.manager.impl.dao.KurjunDataService;


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

    private String localKurjunURL;
    private String glovalKurjunURL;


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

    }


    //****************************************
    public String registerUser(String publicKeyASCII)
    {
        return null;
    }



    //****************************************
    public String authorizeUser(String fingerprint, String signedMessage)
    {
        return null;
    }


    //****************************************
    public boolean setSystemOwner(String fingerprint)
    {
        return true;
    }


    //****************************************
    public String getSystemOwner()
    {
        return null;
    }

}
