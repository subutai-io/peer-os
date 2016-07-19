package io.subutai.core.systemmanager.api.pojo;


public interface NetworkSettings
{
    int getPublicSecurePort();

    void setPublicSecurePort( final int publicSecurePort );

    String getPublicUrl();

    void setPublicUrl( final String publicUrl );

    public int getStartRange();

    public void setStartRange( final int startRange );

    public int getEndRange();

    public void setEndRange( final int endRange );
}
