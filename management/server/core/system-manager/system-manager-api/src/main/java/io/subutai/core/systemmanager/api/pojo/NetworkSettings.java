package io.subutai.core.systemmanager.api.pojo;


public interface NetworkSettings
{
    int getPublicSecurePort();

    void setPublicSecurePort( final int publicSecurePort );

    String getPublicUrl();

    void setPublicUrl( final String publicUrl );
}
