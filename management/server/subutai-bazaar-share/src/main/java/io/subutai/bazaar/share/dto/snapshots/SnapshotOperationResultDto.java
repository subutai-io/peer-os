package io.subutai.bazaar.share.dto.snapshots;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class SnapshotOperationResultDto
{
    private boolean successful;
    private String errors;


    public SnapshotOperationResultDto()
    {
    }


    public SnapshotOperationResultDto( final boolean successful, final String errors )
    {
        this.successful = successful;
        this.errors = errors;
    }


    public boolean isSuccessful()
    {
        return successful;
    }


    public void setSuccessful( final boolean successful )
    {
        this.successful = successful;
    }


    public String getErrors()
    {
        return errors;
    }


    public void setErrors( final String errors )
    {
        this.errors = errors;
    }
}
