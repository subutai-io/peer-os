package io.subutai.core.systemmanager.api.pojo;


public class UpdateDto
{
    private Long updateDate;

    private String prevVersion;

    private String currentVersion;


    public UpdateDto( final Long updateDate, final String prevVersion, final String currentVersion )
    {
        this.updateDate = updateDate;
        this.prevVersion = prevVersion;
        this.currentVersion = currentVersion;
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
}
