package org.safehaus.kiskis.mgmt.api.commandrunner;

import com.google.common.base.Preconditions;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;

/**
 * This class is used to hold commands specific to client.
 * One needs to call {@code init} method prior to using it.
 * Clients should extend this class and add custom commands creation methods
 * e.g.
 * <p/>
 * <blockquote><pre>
 * public class Commands extends CommandsSingleton{
 *   public static Command getStartCommand(Set<Agent> agents) {
 *     return createCommand(
 *       new RequestBuilder("service solr start").withTimeout(60),
 *       agents);
 *   }
 * }
 * </pre></blockquote>
 * <p/>
 * Then somewhere in client code prior to using this class:
 * <p/>
 * {@code Commands.init(commandRunnner);}
 *
 * @author dilshat
 */
public class CommandsSingleton {
    private static CommandsSingleton INSTANCE = new CommandsSingleton();
    private CommandRunner commandRunner;

    protected CommandsSingleton() {
    }

    public final static void init(CommandRunner commandRunner) {
        Preconditions.checkNotNull(commandRunner, "Command Runner is null");
        INSTANCE.commandRunner = commandRunner;
    }

    protected static Command createCommand(RequestBuilder requestBuilder, Set<Agent> agents) {
        return INSTANCE.commandRunner.createCommand(requestBuilder, agents);
    }

    protected static Command createCommand(Set<AgentRequestBuilder> agentRequestBuilders) {
        return INSTANCE.commandRunner.createCommand(agentRequestBuilders);
    }

}
