package io.subutai.hub.share.dto;


public class VehsDto
{
    public enum VehsState
    {
        VERIFY_CHECKSUM,
        DEPLOY,
        DELETE,
        READY,
        COLLECT_METRIC,
        CONFIGURE_DOMAIN,
        WAIT_PEER,
        BUILD_CONTAINER
    }


    private String projectName;
    private String projectOwner;
    private String userName;
    private String userPassword;
    private VehsState state;
    private String data;


    public VehsDto()
    {
    }


    public VehsDto( String projectName, String projectOwner, String userName, String userPassword, VehsState state )
    {
        this.projectName = projectName;
        this.projectOwner = projectOwner;
        this.userName = userName;
        this.userPassword = userPassword;
        this.state = state;
    }


    public String getProjectName()
    {
        return projectName;
    }


    public void setProjectName( final String projectName )
    {
        this.projectName = projectName;
    }


    public String getProjectOwner()
    {
        return projectOwner;
    }


    public void setProjectOwner( final String projectOwner )
    {
        this.projectOwner = projectOwner;
    }


    public String getUserName()
    {
        return userName;
    }


    public void setUserName( final String userName )
    {
        this.userName = userName;
    }


    public String getUserPassword()
    {
        return userPassword;
    }


    public void setUserPassword( final String userPassword )
    {
        this.userPassword = userPassword;
    }


    public VehsState getState()
    {
        return state;
    }


    public void setState( final VehsState state )
    {
        this.state = state;
    }


    public String getData()
    {
        return data;
    }


    public void setData( final String data )
    {
        this.data = data;
    }
}
