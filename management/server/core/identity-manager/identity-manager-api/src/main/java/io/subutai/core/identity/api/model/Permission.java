package io.subutai.core.identity.api.model;

import java.util.List;


public interface Permission
{

    public  void setDelete( boolean delete );

    public  boolean isDelete();

    public  void setUpdate( boolean update );

    public  boolean isUpdate();

    public  void setWrite( boolean write );

    public  boolean Write();

    public  void setRead( boolean read );

    public  boolean isRead();

    public  void setScope( int scope );

    public  int getScope();

    public  void setObject( int object );

    public  int getObject();

    public  void setId( final Long id );

    public  Long getId();

    List<String> asString();

}
