package org.safehaus.subutai.plugin.accumulo.impl.handler.mock;

import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;

public class CommandsMock extends Commands {
    private Command installCommand = null;


    public CommandsMock() {
        super( new CommandRunnerMock() );
    }

    public CommandsMock setInstallCommand( Command command ) {
        this.installCommand = command;
        return this;
    }

}
