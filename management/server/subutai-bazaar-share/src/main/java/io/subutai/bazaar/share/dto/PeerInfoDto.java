package io.subutai.bazaar.share.dto;


import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class PeerInfoDto
{
    private String id;
    private String name;
    private String version;
    private String scope;
    private String commitId;
    private String buildTime;
    private String branch;

    private Set<RhVersionInfoDto> rhVersionInfoDtoList;


    public PeerInfoDto()
    {
    }


    public Set<RhVersionInfoDto> getRhVersionInfoDtoList()
    {
        return rhVersionInfoDtoList;
    }


    public void setRhVersionInfoDtoList( final Set<RhVersionInfoDto> rhVersionInfoDtoList )
    {
        this.rhVersionInfoDtoList = rhVersionInfoDtoList;
    }


    public String getCommitId()
    {
        return commitId;
    }


    public void setCommitId( final String commitId )
    {
        this.commitId = commitId;
    }


    public String getBuildTime()
    {
        return buildTime;
    }


    public void setBuildTime( final String buildTime )
    {
        this.buildTime = buildTime;
    }


    public String getBranch()
    {
        return branch;
    }


    public void setBranch( final String branch )
    {
        this.branch = branch;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getVersion()
    {
        return version;
    }


    public void setVersion( final String version )
    {
        this.version = version;
    }


    public String getScope()
    {
        return scope;
    }


    public void setScope( final String scope )
    {
        this.scope = scope;
    }
}
