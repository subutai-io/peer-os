package org.safehaus.subutai.plugin.common.model;


import java.io.Serializable;


/**
 * Created by talas on 12/9/14.
 */
public class ClusterDataEntityPK implements Serializable
{
    private String source;
    private String id;


    public String getSource()
    {
        return source;
    }


    public void setSource( final String source )
    {
        this.source = source;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof ClusterDataEntityPK ) )
        {
            return false;
        }

        final ClusterDataEntityPK that = ( ClusterDataEntityPK ) o;

        return id.equals( that.id ) && source.equals( that.source );
    }


    @Override
    public int hashCode()
    {
        int result = source.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
