package io.subutai.common.drms;


import java.util.Map;

import com.google.common.collect.Maps;


/**
 * Created by talas on 12/7/15.
 */
public class Relation
{
    private Map<String, Object> properties = Maps.newHashMap();


    public Relation( final Map<String, Object> properties )
    {
        this.properties = properties;
    }


    public Map<String, Object> getProperties()
    {
        return properties;
    }


    public void setProperties( final Map<String, Object> properties )
    {
        this.properties = properties;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof Relation ) )
        {
            return false;
        }

        final Relation relation = ( Relation ) o;

        return !( properties != null ? !properties.equals( relation.properties ) : relation.properties != null );
    }


    @Override
    public int hashCode()
    {
        return properties != null ? properties.hashCode() : 0;
    }


    @Override
    public String toString()
    {
        return "Relation{" +
                "properties=" + properties +
                '}';
    }
}
