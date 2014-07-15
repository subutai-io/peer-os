package org.safehaus.subutai.impl.fstracker;


import com.google.common.base.Preconditions;
import org.safehaus.subutai.api.communicationmanager.CommunicationManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;

public class FSTrackerImpl {

    private CommunicationManager communicationManager;
    private CommandRunner commandRunner;

    public void setCommandRunner(CommandRunner commandRunner) {
    		this.commandRunner = commandRunner;
    	}

    public FSTrackerImpl( CommunicationManager communicationManager, CommandRunner commandRunner ) {
        Preconditions.checkNotNull( communicationManager, "CommunicationManager is null" );

        this.communicationManager = communicationManager;
        this.commandRunner = commandRunner;
    }


}
