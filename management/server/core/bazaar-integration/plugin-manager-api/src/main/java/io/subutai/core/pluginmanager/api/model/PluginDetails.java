package io.subutai.core.pluginmanager.api.model;


public interface PluginDetails
{
    public Long getId();

    public void setId( final Long id );

    public String getName();

    public void setName( final String name );

    public String getKar();

    public void setKar( final String kar );


    public String getVersion();

    public void setVersion( final String version );
}
