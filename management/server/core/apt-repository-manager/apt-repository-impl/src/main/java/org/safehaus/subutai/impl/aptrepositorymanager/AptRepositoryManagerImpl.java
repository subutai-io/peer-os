/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.aptrepositorymanager;


import java.util.List;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepoException;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepositoryManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.base.Preconditions;


/**
 * This is an implementation of AptRepositoryManager
 */
public class AptRepositoryManagerImpl implements AptRepositoryManager {
    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private Agent managementAgent;


    public AptRepositoryManagerImpl( final CommandRunner commandRunner, final AgentManager agentManager ) {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        managementAgent = agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME );
    }


    @Override
    public List<String> listPackages( final String pattern ) throws AptRepoException {
        return null;
    }


    @Override
    public void addPackageToRepo( final String pathToPackageFile ) throws AptRepoException {

    }


    @Override
    public void removePackageByFilePath( final String packageFileName ) throws AptRepoException {

    }


    @Override
    public void removePackageByName( final String packageName ) throws AptRepoException {

    }


    @Override
    public String readFileContents( final String pathToFileInsideDebPackage ) throws AptRepoException {
        return null;
    }
}
