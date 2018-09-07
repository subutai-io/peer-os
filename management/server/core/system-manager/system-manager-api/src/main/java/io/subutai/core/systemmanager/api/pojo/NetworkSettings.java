package io.subutai.core.systemmanager.api.pojo;


public interface NetworkSettings
{
    int getPublicSecurePort();


    String getPublicUrl();


    int getStartRange();


    int getEndRange();


    String getBazaarIp();

    boolean getUseRhIp();
}
