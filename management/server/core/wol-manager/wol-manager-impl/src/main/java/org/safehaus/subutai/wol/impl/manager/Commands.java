package org.safehaus.subutai.wol.impl.manager;


import org.safehaus.subutai.common.command.RequestBuilder;


/**
 * Created by ebru on 08.12.2014.
 */
public class Commands
{
    public static String PACKAGE_POSTFIX = "-subutai-plugin";

    public static String PACKAGE_POSTFIX_WITHOUT_DASH = "subutai-plugin";

    public static String INSTALL_COMMAND = "apt-get --force-yes --assume-yes install ";

    public static String PURGE_COMMAND = "apt-get --force-yes --assume-yes purge ";

    public static String UPGRADE_COMMAND = "apt-get --force-yes --assume-yes upgrade ";

    public static String CHECK_COMMAND = "dpkg -l | grep '^ii' | grep " + PACKAGE_POSTFIX_WITHOUT_DASH;

    public String makePackageName( String pluginName)
    {
        String packageName = pluginName + PACKAGE_POSTFIX;

        return packageName;
    }

    public RequestBuilder makeInstallCommand( String pluginName )
    {
        String command = INSTALL_COMMAND + makePackageName( pluginName );

        return new RequestBuilder( command ).withTimeout( 600 );
    }

    public RequestBuilder makeRemoveCommand( String pluginName)
    {
        String command = PURGE_COMMAND + makePackageName( pluginName );

        return new RequestBuilder( command ).withTimeout( 600 );
    }

    public RequestBuilder makeUpgradeCommand ( String pluginName )
    {
        String command = UPGRADE_COMMAND + makePackageName( pluginName );

        return new RequestBuilder( command ).withTimeout( 600 );
    }

    public RequestBuilder makeCheckCommand()
    {
        return new RequestBuilder( CHECK_COMMAND );
    }
}
