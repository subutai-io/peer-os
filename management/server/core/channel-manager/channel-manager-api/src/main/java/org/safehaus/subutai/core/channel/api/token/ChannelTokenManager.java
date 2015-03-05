package org.safehaus.subutai.core.channel.api.token;


import javax.persistence.EntityManagerFactory;

import org.safehaus.subutai.core.channel.api.entity.IUserChannelToken;


/**
 * Created by nisakov on 3/3/15.
 */
public interface ChannelTokenManager
{

    /***********************************************************************************************************
     *
     * */
    public EntityManagerFactory getEntityManagerFactory();

    /***********************************************************************************************************
     *
     * */
    public void setEntityManagerFactory( EntityManagerFactory entityManagerFactory );

    /***********************************************************************************************************
     *
     * */
    public long getUserChannelToken(String token);

    /***********************************************************************************************************
     *
     * */
    public void setTokenValidity();


    /***********************************************************************************************************
     *
     * */
    public void saveUserChannelToken(IUserChannelToken obj);

    /***********************************************************************************************************
     *
     * */
    public  IUserChannelToken getUserChannelTokenData(long userId);


    /***********************************************************************************************************
     *
     * */
    public IUserChannelToken createUserChannelToken();


}
