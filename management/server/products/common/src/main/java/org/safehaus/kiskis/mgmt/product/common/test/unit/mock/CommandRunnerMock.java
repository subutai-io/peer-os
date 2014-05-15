package org.safehaus.kiskis.mgmt.product.common.test.unit.mock;


import java.util.Set;
import java.util.UUID;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentRequestBuilder;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.CommandMock;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;


public class CommandRunnerMock implements CommandRunner {

    @Override
    public Command createCommand( RequestBuilder requestBuilder, Set<Agent> agents ) {
        Request request = requestBuilder.build( UUID.randomUUID(), UUID.randomUUID() );
        return new CommandMock().setDescription( request.getProgram() );
    }


    @Override
    public Command createCommand( String description, RequestBuilder requestBuilder, Set<Agent> agents ) {
        return null;
    }


    @Override
    public Command createCommand( Set<AgentRequestBuilder> agentRequestBuilders ) {
        return null;
    }


    @Override
    public Command createCommand( String description, Set<AgentRequestBuilder> agentRequestBuilders ) {
        return null;
    }


    @Override
    public void runCommandAsync( Command command, CommandCallback commandCallback ) {

    }


    @Override
    public void runCommandAsync( Command command ) {

    }


    @Override
    public void runCommand( Command command ) {

    }


    @Override
    public void runCommand( Command command, CommandCallback commandCallback ) {

    }
}
