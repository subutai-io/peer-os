package io.subutai.core.identity.api.model;


import java.io.Serializable;
import java.util.List;


public interface Permission extends Serializable
{

    void setDelete( boolean delete );

    boolean isDelete();

    void setUpdate( boolean update );

    boolean isUpdate();

    void setWrite( boolean write );

    boolean isWrite();

    void setRead( boolean read );

    boolean isRead();

    void setScope( int scope );

    int getScope();

    void setObject( int object );

    int getObject();

    void setId( final Long id );

    Long getId();

    String getObjectName();

    List<String> asString();
}
