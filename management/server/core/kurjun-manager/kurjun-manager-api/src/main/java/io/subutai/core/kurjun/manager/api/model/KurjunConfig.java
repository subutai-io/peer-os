package io.subutai.core.kurjun.manager.api.model;


public interface KurjunConfig
{
    public Long getId();

    public void setId( final Long id );

    public String getOwnerFingerprint();

    public void setOwnerFingerprint( final String ownerFingerprint );

    public String getAuthID();

    public void setAuthID( final String authID );

    public int getType();

    public void setType( final int type );
}
