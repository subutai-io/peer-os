package io.subutai.core.desktop.impl;


import io.subutai.common.command.RequestBuilder;


public class Commands
{
    private static final String DESKTOP_ENV_VALUE = "echo $XDG_CURRENT_DESKTOP"; //
    private static final String X2GO_SERVER_SEARCH = "service --status-all | grep x2goserver";


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
}
