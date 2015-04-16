package org.safehaus.subutai.pluginmanager.impl;


import org.safehaus.subutai.common.command.RequestBuilder;


public class Commands
{
    public static String PACKAGE_POSTFIX = "-subutai-plugin";

    public static String PACKAGE_POSTFIX_WITHOUT_DASH = "subutai-plugin";

    public static String INSTALL_COMMAND = "apt-get --force-yes --assume-yes install ";

    public static String PURGE_COMMAND = "apt-get --force-yes --assume-yes purge ";

    public static String UPGRADE_COMMAND = "apt-get --force-yes --assume-yes upgrade ";

    public static String CHECK_COMMAND = "dpkg -l | grep '^ii' | grep " + PACKAGE_POSTFIX_WITHOUT_DASH;


    public String makePackageName( String pluginName )
    {
        String packageName = pluginName + PACKAGE_POSTFIX;

        return packageName;
    }


    public RequestBuilder makeInstallCommand( String pluginName )
    {
        String command = INSTALL_COMMAND + makePackageName( pluginName );

        return new RequestBuilder( command ).withTimeout( 600 );
    }


    public RequestBuilder makeRemoveCommand( String pluginName )
    {
        String command = PURGE_COMMAND + makePackageName( pluginName );

        return new RequestBuilder( command ).withTimeout( 600 );
    }


    public RequestBuilder makeUpgradeCommand( String pluginName )
    {
        String command = UPGRADE_COMMAND + makePackageName( pluginName );

        return new RequestBuilder( command ).withTimeout( 600 );
    }


    public RequestBuilder makeCheckCommand()
    {
        return new RequestBuilder( CHECK_COMMAND );
    }

    public RequestBuilder makeCheckIfInstalledCommand()
    {
        return new RequestBuilder( String.format( "dpkg-query -W *%s | grep -v repo", PACKAGE_POSTFIX ) );
    }

    public RequestBuilder makeListLocalPluginsCommand()
    {
        return new RequestBuilder( String.format( "apt-cache search %s | grep -v repo | awk -F '-' '{ print $1 }' ", PACKAGE_POSTFIX_WITHOUT_DASH ) );
    }

    public RequestBuilder makeIsInstalledCommand( String pluginName )
    {
        //return new RequestBuilder( String.format( "dpkg-query -W %s-%s | grep -v repo", pluginName,PACKAGE_POSTFIX_WITHOUT_DASH ) );
        return new RequestBuilder( String.format( "dpkg -s %s-%s", pluginName,PACKAGE_POSTFIX_WITHOUT_DASH ) );
    }
}
