package io.subutai.bazaar.share.dto.backup;


import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class BackupCommandResultDto
{
    private String backupCdnId;
    private String encryptionPassword;
    private Date snapshotDate;
    private String error;


    public BackupCommandResultDto()
    {
    }


    public BackupCommandResultDto( final String backupCdnId, final String encryptionPassword, final Date snapshotDate,
                                   final String error )
    {
        this.backupCdnId = backupCdnId;
        this.encryptionPassword = encryptionPassword;
        this.snapshotDate = snapshotDate;
        this.error = error;
    }


    public String getBackupCdnId()
    {
        return backupCdnId;
    }


    public void setBackupCdnId( final String backupCdnId )
    {
        this.backupCdnId = backupCdnId;
    }


    public String getEncryptionPassword()
    {
        return encryptionPassword;
    }


    public void setEncryptionPassword( final String encryptionPassword )
    {
        this.encryptionPassword = encryptionPassword;
    }


    public Date getSnapshotDate()
    {
        return snapshotDate;
    }


    public void setSnapshotDate( final Date snapshotDate )
    {
        this.snapshotDate = snapshotDate;
    }


    public String getError()
    {
        return error;
    }


    public void setError( final String error )
    {
        this.error = error;
    }
}
