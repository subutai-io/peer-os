package io.subutai.bazaar.share.dto.backup;


import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.subutai.bazaar.share.quota.ContainerQuota;


@JsonIgnoreProperties( ignoreUnknown = true )
public class ContainerRestoreCommandDto
{
    private ArrayList<CdnBackupFileDto> backupFileSequence = new ArrayList<>();

    private String containerOldName;
    private String containerHostname;
    private String resourceHostId;
    private String templateId;
    private ContainerQuota containerQuota;
    private String containerIpAddress;
    private int vlan;

    private boolean destroyOldContainer;

    private RestoreCommandResultDto result = new RestoreCommandResultDto();


    public ContainerRestoreCommandDto()
    {
    }


    public ArrayList<CdnBackupFileDto> getBackupFileSequence()
    {
        return backupFileSequence;
    }


    public void addBackupFile( final CdnBackupFileDto backupFile )
    {
        this.backupFileSequence.add( backupFile );
    }


    public String getContainerOldName()
    {
        return containerOldName;
    }


    public void setContainerOldName( final String containerOldName )
    {
        this.containerOldName = containerOldName;
    }


    public String getContainerHostname()
    {
        return containerHostname;
    }


    public void setContainerHostname( final String containerHostname )
    {
        this.containerHostname = containerHostname;
    }


    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public void setResourceHostId( final String resourceHostId )
    {
        this.resourceHostId = resourceHostId;
    }


    public String getTemplateId()
    {
        return templateId;
    }


    public void setTemplateId( final String templateId )
    {
        this.templateId = templateId;
    }


    public ContainerQuota getContainerQuota()
    {
        return containerQuota;
    }


    public void setContainerQuota( final ContainerQuota containerQuota )
    {
        this.containerQuota = containerQuota;
    }


    public String getContainerIpAddress()
    {
        return containerIpAddress;
    }


    public void setContainerIpAddress( final String containerIpAddress )
    {
        this.containerIpAddress = containerIpAddress;
    }


    public int getVlan()
    {
        return vlan;
    }


    public void setVlan( final int vlan )
    {
        this.vlan = vlan;
    }


    public boolean isDestroyOldContainer()
    {
        return destroyOldContainer;
    }


    public void setDestroyOldContainer( final boolean destroyOldContainer )
    {
        this.destroyOldContainer = destroyOldContainer;
    }


    public RestoreCommandResultDto getResult()
    {
        return result;
    }


    public void setResult( final RestoreCommandResultDto restoreCommandResultDto )
    {
        this.result = restoreCommandResultDto;
    }


    @Override
    public String toString()
    {
        return "ContainerRestoreCommandDto{containerOldName='" + containerOldName + "', containerHostname='"
                + containerHostname + "', resourceHostId='" + resourceHostId + "', templateId='" + templateId
                + "', containerQuota=" + containerQuota + ", containerIpAddress='" + containerIpAddress + "', vlan="
                + vlan + ", destroyOldContainer=" + destroyOldContainer + '}';
    }
}
