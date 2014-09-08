package org.safehaus.subutai.plugin.accumulo.impl.handler.mock;

import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;

import static org.mockito.Mockito.mock;

public class CommandsMock extends Commands {
    private Command installCommand = null;


    public CommandsMock() {
        super( mock( CommandRunner.class) );
    }

    public CommandsMock setInstallCommand( Command command ) {
        this.installCommand = command;
        return this;
    }

}
