package io.subutai.core.identity.api.model;

import java.util.List;

import io.subutai.core.identity.api.model.Permission;


public interface Role
{

    public abstract void setType( final int type );

    public abstract int getType();

    public abstract void setName( final String name );

    public abstract String getName();

    public abstract void setId( final Long id );

    public abstract Long getId();

    String getTypeName();

    List<Permission> getPermissions();

    void setPermissions( List<Permission> permissions );

}

