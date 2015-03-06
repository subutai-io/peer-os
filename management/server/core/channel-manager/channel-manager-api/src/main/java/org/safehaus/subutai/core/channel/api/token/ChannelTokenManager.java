package org.safehaus.subutai.core.channel.api.token;


import java.util.List;

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
    public long getUserChannelTokenId(String token);

    /***********************************************************************************************************
     *
     * */
    public IUserChannelToken getUserChannelToken(String token);
    /***********************************************************************************************************
     *
     * */
    public void setTokenValidity();

    /***********************************************************************************************************
     *
     * */
    public void removeUserChannelToken(String token);
    /***********************************************************************************************************
     *
     * */
    public void saveUserChannelToken(IUserChannelToken obj);

    /***********************************************************************************************************
     *
     * */
    public List<IUserChannelToken> getUserChannelTokenData(long userId);


    /***********************************************************************************************************
     *
     * */
    List<IUserChannelToken> getAllUserChannelTokenData();


    /***********************************************************************************************************
     *
     * */
    public IUserChannelToken createUserChannelToken();


}
