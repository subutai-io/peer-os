package io.subutai.core.identity.api.model;


import java.io.Serializable;
import java.util.Set;


public interface Role extends Serializable
{
    Long getId();

    void setId( Long id );

    String getName();

    void setName( String name );

    Short getType();

    void setType( Short type );

    Set<User> getAssignedUsers();

    void setAssignedUsers( Set<User> assignedUsers );
}
