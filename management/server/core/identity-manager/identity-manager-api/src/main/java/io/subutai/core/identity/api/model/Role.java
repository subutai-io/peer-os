package io.subutai.core.identity.api.model;


import java.util.List;


public interface Role
{

    void setType( final int type );

    int getType();

    void setName( final String name );

    String getName();

    void setId( final Long id );

    Long getId();

    String getTypeName();

    // TODO: delete methods related to join table
    List<Permission> getPermissions();

    void setPermissions( List<Permission> permissions );
}

