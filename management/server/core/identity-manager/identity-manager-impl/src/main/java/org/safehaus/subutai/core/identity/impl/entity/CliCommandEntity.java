package org.safehaus.subutai.core.identity.impl.entity;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.safehaus.subutai.core.identity.api.CliCommand;

import com.google.common.base.Preconditions;


/**
 * Created by talas on 3/13/15.
 */
@Entity
@Table( name = "cli_command_entity" )
@Access( AccessType.FIELD )
@IdClass( CliCommandPK.class )
public class CliCommandEntity implements CliCommand
{
    @Id
    @Column( name = "cli_scope" )
    private String scope;

    @Id
    @Column( name = "cli_name" )
    private String name;


    public CliCommandEntity()
    {
    }


    public CliCommandEntity( final String scope, final String name )
    {
        Preconditions.checkNotNull( scope, "Scope cannot be null" );
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
