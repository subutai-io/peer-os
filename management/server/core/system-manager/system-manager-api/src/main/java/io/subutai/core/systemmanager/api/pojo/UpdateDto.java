package io.subutai.core.systemmanager.api.pojo;


public class UpdateDto
{
    private Long updateDate;

    private String prevVersion;

    private String currentVersion;

    private String prevCommitId;

    private String currentCommitId;


    public UpdateDto( final Long updateDate, final String prevVersion, final String currentVersion,
                      final String prevCommitId, final String currentCommitId )
    {
        this.updateDate = updateDate;
        this.prevVersion = prevVersion;
        this.currentVersion = currentVersion;
        this.prevCommitId = prevCommitId;
        this.currentCommitId = currentCommitId;
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


    public String getPrevCommitId()
    {
        return prevCommitId;
    }


    public String getCurrentCommitId()
    {
        return currentCommitId;
    }
}
