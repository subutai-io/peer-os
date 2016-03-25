package io.subutai.core.kurjun.manager.api;


import io.subutai.core.kurjun.manager.api.dao.KurjunDataService;


/**
 *
 */
public interface KurjunManager
{

    //****************************************
    String registerUser( int kurjunType, String fingerprint );


    //****************************************
    String authorizeUser( int kurjunType, String fingerprint );


    //****************************************
    boolean setSystemOwner( int kurjunType, String fingerprint );


    //****************************************
    String getSystemOwner( int kurjunType );


    //****************************************
    String getUser( int kurjunType, String fingerprint );

    public KurjunDataService getDataService();
}
