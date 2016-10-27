package io.subutai.core.pluginmanager.api.model;


//todo remove all mutators from interface
public interface PluginDetails
{
    Long getId();

    void setId( final Long id );

    String getName();

    void setName( final String name );

    String getKar();

    void setKar( final String kar );

    String getVersion();

    void setVersion( final String version );
}
