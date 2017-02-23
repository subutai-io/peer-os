package io.subutai.core.systemmanager.api.pojo;


//todo remove mutators from interface
public interface NetworkSettings
{
    int getPublicSecurePort();

    void setPublicSecurePort( final int publicSecurePort );

    String getPublicUrl();

    void setPublicUrl( final String publicUrl );

    int getStartRange();

    void setStartRange( final int startRange );

    int getEndRange();

    void setEndRange( final int endRange );

    String getHubIp();

    void setHubIp( final String hubIp );
}
