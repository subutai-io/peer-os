/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mahout.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.CommandsSingleton;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;


public class Commands extends CommandsSingleton
{
    public static final String PACKAGE_NAME = "ksks-mahout";


    public Commands( CommandRunner commandRunner )
    {
        init( commandRunner );
    }


    public static Command getInstallCommand( Set<Agent> agents )
    {
        return createCommand( "Install Mahout",
                new RequestBuilder( "apt-get --force-yes --assume-yes install ksks-mahout" ).withTimeout( 360 )
                                                                                            .withStdOutRedirection(
                                                                                                    OutputRedirection
                                                                                                            .NO ),
                agents );
    }


    public static Command getUninstallCommand( Set<Agent> agents )
    {
        return createCommand( "Uninstall Mahout",
                new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-mahout" ).withTimeout( 60 ), agents );
    }


    public static Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return createCommand( "Check installed ksks packages", new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ),
                agents );
    }
}
