package io.subutai.bazaar.share.dto.backup;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class CdnBackupFileDto
{
    private String cdnId;
    private String fileName;
    private String password;


    public CdnBackupFileDto()
    {
    }


    public CdnBackupFileDto( final String cdnId, final String fileName, final String password )
    {
        this.cdnId = cdnId;
        this.fileName = fileName;
        this.password = password;
    }


    public String getCdnId()
    {
        return cdnId;
    }


    public void setCdnId( final String cdnId )
    {
        this.cdnId = cdnId;
    }


    public String getFileName()
    {
        return fileName;
    }


    public void setFileName( final String fileName )
    {
        this.fileName = fileName;
    }


    public String getPassword()
    {
        return password;
    }


    public void setPassword( final String password )
    {
        this.password = password;
    }
}
