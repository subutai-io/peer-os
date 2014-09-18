package org.safehaus.subutai.plugin.elasticsearch.impl;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.core.command.api.CommandsSingleton;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Set;

public class Commands extends CommandsSingleton {

	public static Command getInstallCommand(Set<Agent> agents) {

		return createCommand(
				new RequestBuilder(
						"sleep 10; apt-get --force-yes --assume-yes install ksks-elasticsearch")
						.withTimeout(90).withStdOutRedirection( OutputRedirection.NO),
				agents
		);

	}


    public static Command getUninstallCommand( Set<Agent> agents ) {
        return createCommand( "Uninstall Mahout",
                new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-elasticsearch" ).withTimeout( 60 ),
                agents );
    }


	public static Command getConfigureCommand(Set<Agent> agents, String param) {
		return createCommand(new RequestBuilder(String.format(" . /etc/profile && es-conf.sh %s ", param)), agents );
	}


	public static Command getStatusCommand(Set<Agent> agents) {
		return createCommand(new RequestBuilder("service elasticsearch status"), agents);
	}


	public static Command getStartCommand(Set<Agent> agents) {
		return createCommand(new RequestBuilder("service elasticsearch start"), agents);
	}


	public static Command getStopCommand(Set<Agent> agents) {
		return createCommand(new RequestBuilder("service elasticsearch stop"), agents);
	}


    public static Command getCheckInstalledCommand( Set<Agent> agents ) {
        return createCommand( "Check installed ksks packages", new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ),
                agents );
    }

}
