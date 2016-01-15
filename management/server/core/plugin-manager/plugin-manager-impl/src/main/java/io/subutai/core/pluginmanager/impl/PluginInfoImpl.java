package io.subutai.core.pluginmanager.impl;


import io.subutai.core.pluginmanager.api.PluginInfo;


public class PluginInfoImpl implements PluginInfo
{
    private String type;
    private String pluginName;
    private String version;
    private String rating;


    @Override
    public String getVersion()
    {
        return version;
    }


    @Override
    public void setVersion( final String version )
    {
        this.version = version;
    }


    @Override
    public String getPluginName()
    {
        return pluginName;
    }


    @Override
    public void setPluginName( final String pluginName )
    {
        this.pluginName = pluginName;
    }


    @Override
    public String getType()
    {
        return type;
    }


    @Override
    public void setType( final String type )
    {
        this.type = type;
    }


    @Override
    public String getRating()
    {
        return rating;
    }


    @Override
    public void setRating( final String rating )
    {
        this.rating = rating;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof PluginInfoImpl ) )
        {
            return false;
        }

        final PluginInfoImpl that = ( PluginInfoImpl ) o;

        if ( pluginName != null ? !pluginName.equals( that.pluginName ) : that.pluginName != null )
        {
            return false;
        }
        return !( version != null ? !version.equals( that.version ) : that.version != null );
    }


    @Override
    public int hashCode()
    {
        int result = pluginName != null ? pluginName.hashCode() : 0;
        result = 31 * result + ( version != null ? version.hashCode() : 0 );
        return result;
    }


    @Override
    public String toString()
    {
        return "PluginInfoImpl{" +
                "type='" + type + '\'' +
                ", pluginName='" + pluginName + '\'' +
                ", version='" + version + '\'' +
                ", rating='" + rating + '\'' +
                '}';
    }
}
