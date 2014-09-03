/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.configuration.impl.command;


import com.google.common.collect.Sets;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.CommandsSingleton;
import org.safehaus.subutai.core.commandrunner.api.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.enums.OutputRedirection;


/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

	public static Command getCatCommand(Agent agent, String filePath) {

		return createCommand(new RequestBuilder("cat " + filePath).withTimeout(90).withStdOutRedirection(
				OutputRedirection.CAPTURE_AND_RETURN), Sets.newHashSet(agent));
	}


	public static Command getEchoCommand(Agent agent, String filePath, String content) {

		return createCommand(new RequestBuilder("echo " + " '" + content + "' > " + filePath)
						.withTimeout(90)
						.withStdOutRedirection(OutputRedirection.CAPTURE_AND_RETURN),
				Sets.newHashSet(agent));
	}
}
