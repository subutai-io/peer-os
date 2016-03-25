package io.subutai.core.kurjun.manager.api.model;


/**
 *
 */
public interface Kurjun
{
    public String getUrl();

    public void setUrl( final String url );

    public boolean getState();

    public void setState( final boolean state );

    long getId();

    void setId( long id );

    int getType();

    void setType( int type );
}
