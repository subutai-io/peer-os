package io.subutai.bazaar.share.dto.backup;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class RestoreCommandResultDto
{
    private String restoredContainerId;
    private String restoredContainerName;
    private Set<String> sshKeys = new HashSet<>();


    public RestoreCommandResultDto()
    {
    }


    public RestoreCommandResultDto( final String restoredContainerId, final String restoredContainerName )
    {
        this.restoredContainerId = restoredContainerId;
        this.restoredContainerName = restoredContainerName;
    }


    public String getRestoredContainerId()
    {
        return restoredContainerId;
    }


    public void setRestoredContainerId( final String restoredContainerId )
    {
        this.restoredContainerId = restoredContainerId;
    }


    public String getRestoredContainerName()
    {
        return restoredContainerName;
    }


    public void setRestoredContainerName( final String restoredContainerName )
    {
        this.restoredContainerName = restoredContainerName;
    }


    public void setSshKeys( final Set<String> sshKeys )
    {
        this.sshKeys = sshKeys;
    }


    public Set<String> getSshKeys()
    {
        return sshKeys;
    }


    public void addSshKey( final String sshKey )
    {
        if ( this.sshKeys != null )
        {
            this.sshKeys.add( sshKey );
        }
        else
        {
            this.sshKeys = new HashSet<>();
            this.sshKeys.add( sshKey );
        }
    }
}
