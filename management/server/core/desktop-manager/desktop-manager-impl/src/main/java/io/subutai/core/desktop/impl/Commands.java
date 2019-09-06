package io.subutai.core.desktop.impl;


import io.subutai.common.command.RequestBuilder;


public class Commands
{
    private static final String DESKTOP_ENV_VALUE = "pgrep -l 'x2go' | awk '{print $2}'"; //
    private static final String X2GO_SERVER_SEARCH = "service --status-all | grep x2goserver";
    private static final String USER = "x2go";


    /**
     * @return desktop environment (ex: Mate, Gnome, Unity, etc) specify command
     */
    public static RequestBuilder getDeskEnvSpecifyCommand()
    {
        return new RequestBuilder( DESKTOP_ENV_VALUE );
    }


    /**
     * @return remote desktop (RD) specify command
     */
    public static RequestBuilder getRDServerSpecifyCommand()
    {
        return new RequestBuilder( X2GO_SERVER_SEARCH );
    }


    /**
     * @return create default .ssh directory command
     */
    public static RequestBuilder getCreateDefaultSSHDirectoryCommand()
    {
        return new RequestBuilder( String.format( "mkdir /home/%s/.ssh", USER ) );
    }


    /**
     * @return create remote desktop (RD) user
     */
    public static RequestBuilder getCreateDesktopUserCommand()
    {
        return new RequestBuilder( String.format( "sudo useradd -m %s", USER ) );
    }


    /**
     * @return copy authenticated_keys to RD user home file command
     */
    public static RequestBuilder getCopyAuthKeysCommand()
    {
        return new RequestBuilder( String.format( "cp ~/.ssh/authorized_keys /home/%s/.ssh/", USER ) );
    }
}
