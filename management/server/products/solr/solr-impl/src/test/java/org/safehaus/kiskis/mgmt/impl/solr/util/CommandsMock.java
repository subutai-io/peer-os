package org.safehaus.kiskis.mgmt.impl.solr.util;


import java.util.Set;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.impl.solr.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;


public class CommandsMock extends Commands {

    private Command installCommand = new CommandMock().setDescription( INSTALL );
    private Command startCommand = new CommandMock().setDescription( START );
    private Command stopCommand = new CommandMock().setDescription( STOP );
    private Command statusCommand = new CommandMock().setDescription( STATUS );


    public CommandsMock() {
        super( new CommandRunnerMock() );
    }


    @Override
    public Command getInstallCommand( Set<Agent> agents ) {
        return installCommand;
    }


    public CommandsMock setInstallCommand( Command command ) {
        this.installCommand = command;
        return this;
    }


    @Override
    public Command getStartCommand( Agent agent ) {
        return startCommand;
    }


    @Override
    public Command getStopCommand( Agent agent ) {
        return stopCommand;
    }


    @Override
    public Command getStatusCommand( Agent agent ) {
        return statusCommand;
    }

}
