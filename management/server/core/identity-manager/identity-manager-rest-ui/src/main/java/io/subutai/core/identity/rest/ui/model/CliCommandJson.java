package io.subutai.core.identity.rest.ui.model;

import com.google.common.base.Preconditions;
import io.subutai.core.identity.api.CliCommand;

public class CliCommandJson implements CliCommand
{
    private String scope;

    private String name;

    public CliCommandJson( final String scope, final String name )
    {
        Preconditions.checkNotNull(scope, "Scope cannot be null");
        Preconditions.checkNotNull( name, "Name cannot be null" );
        this.scope = scope;
        this.name = name;
    }

    @Override
    public String getScope()
    {
        return scope;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getCommand()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return String.format( "%s:%s", scope, name );
    }
}
