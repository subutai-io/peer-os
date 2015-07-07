package io.subutai.core.identity.impl.entity;


import com.google.common.base.Preconditions;


public class CliCommandPK
{
    private String scope;
    private String name;


    public CliCommandPK()
    {
    }


    public CliCommandPK( final String scope, final String name )
    {
        Preconditions.checkNotNull( scope, "Invalid argument scope" );
        Preconditions.checkNotNull( name, "Invalid argument name" );
        this.scope = scope;
        this.name = name;
    }


    public String getScope()
    {
        return scope;
    }


    public String getName()
    {
        return name;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof CliCommandPK ) )
        {
            return false;
        }

        final CliCommandPK that = ( CliCommandPK ) o;

        return name.equals( that.name ) && scope.equals( that.scope );
    }


    @Override
    public int hashCode()
    {
        int result = scope.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
