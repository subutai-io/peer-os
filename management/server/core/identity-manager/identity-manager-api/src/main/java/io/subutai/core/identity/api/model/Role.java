package io.subutai.core.identity.api.model;

import java.util.List;

import io.subutai.core.identity.api.model.Permission;


public interface Role
{

    public abstract void setType( final Short type );

    public abstract Short getType();

    public abstract void setName( final String name );

    public abstract String getName();

    public abstract void setId( final Long id );

    public abstract Long getId();

    List<Permission> getPermissions();

    void setPermissions( List<Permission> permissions );

}

