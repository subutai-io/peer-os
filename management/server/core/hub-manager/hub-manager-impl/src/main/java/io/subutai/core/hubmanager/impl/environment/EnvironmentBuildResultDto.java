package io.subutai.core.hubmanager.impl.environment;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * TODO. Move to subutai-hub-share
 */
public class EnvironmentBuildResultDto
{
    private String envId;

    private boolean success;

    private String message;


    // The default constructor is required for CBOR
    public EnvironmentBuildResultDto()
    {
    }


    public EnvironmentBuildResultDto( String envId, boolean success, String message )
    {
        this.envId = envId;
        this.success = success;
        this.message = message;
    }


    public String getEnvironmentId()
    {
        return envId;
    }


    public boolean isSuccess()
    {
        return success;
    }


    public String getMessage()
    {
        return message;
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE )
                .append( "envId", envId )
                .append( "success", success )
                .append( "message", message )
                .toString();
    }
}
