package io.subutai.core.systemmanager.impl.entity;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


@Entity
@Table( name = "update_history" )
@Access( AccessType.FIELD )
public class UpdateEntity
{
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long updateDate;

    @Column
    private String prevVersion;

    @Column
    private String currentVersion;


    public UpdateEntity()
    {
    }


    public UpdateEntity( final String prevVersion )
    {
        this.prevVersion = prevVersion;
        this.updateDate = System.currentTimeMillis();
    }


    public Long getUpdateDate()
    {
        return updateDate;
    }


    public String getPrevVersion()
    {
        return prevVersion;
    }


    public String getCurrentVersion()
    {
        return currentVersion;
    }


    public void setCurrentVersion( final String currentVersion )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( currentVersion ) );

        this.currentVersion = currentVersion;
    }
}
