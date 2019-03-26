package io.subutai.bazaar.share.dto.snapshots;


import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class SnapshotDto
{
    private String containerId;
    private String label;
    private Date createDate;


    public SnapshotDto()
    {
    }


    public SnapshotDto( final String containerId, final String label, final Date createDate )
    {
        this.containerId = containerId;
        this.label = label;
        this.createDate = createDate;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public void setContainerId( final String containerId )
    {
        this.containerId = containerId;
    }


    public String getLabel()
    {
        return label;
    }


    public void setLabel( final String label )
    {
        this.label = label;
    }


    public Date getCreateDate()
    {
        return createDate;
    }


    public void setCreateDate( final Date createDate )
    {
        this.createDate = createDate;
    }
}
