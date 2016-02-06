package io.subutai.core.identity.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.UserDelegate;


/**
 * Delegate for User
 */
@Entity
@Table( name = "user_delegate" )
@Access( AccessType.FIELD )
public class UserDelegateEntity implements UserDelegate
{
    @Id
    @Column( name = "id" )
    private String id;

    @Column( name = "user_id")
    private long userId;

    @Column( name = "type" )
    private int type = 2; // System User


    @Override
    public String getId()
    {
        return id;
    }


    @Override
    public void setId( final String id )
    {
        this.id = id;
    }


    @Override
    public long getUserId()
    {
        return userId;
    }


    @Override
    public void setUserId( final long userId )
    {
        this.userId = userId;
    }


    @Override
    public int getType()
    {
        return type;
    }


    @Override
    public void setType( final int type )
    {
        this.type = type;
    }

    @Override
    public String getLinkId()
    {
        return String.format("%s|%s", getClassPath(), getUniqueIdentifier() );
    }


    @Override
    public String getUniqueIdentifier()
    {
        return getId();
    }


    @Override
    public String getClassPath()
    {
        return this.getClass().getSimpleName();
    }


    @Override
    public String getContext()
    {
        return "LocalPeerManager";
    }
}
