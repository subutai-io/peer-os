package org.safehaus.subutai.cli.container.manager;

import java.util.Arrays;
import java.util.Set;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.template.manager.TemplateManager;
import org.safehaus.subutai.shared.protocol.Agent;

@Command(scope = "container", name = "clone-many")
public class CloneMany extends OsgiCommandSupport {

    TemplateManager templateManager;
    ContainerManager containerManager;
    AgentManager agentManager;

    @Argument(index = 0, required = true)
    private String template;
    @Argument(index = 1, required = true)
    private int nodesCount;
    @Argument(index = 2, required = true)
    private String hosts;

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public void setContainerManager(ContainerManager containerManager) {
        this.containerManager = containerManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    @Override
    protected Object doExecute() throws Exception {

        Agent a = agentManager.getAgentByHostname(hosts);
        Set<Agent> set = containerManager.clone("my_group", template, nodesCount, Arrays.asList(a));
        if(set.isEmpty()) System.out.println("Empty set");
        else System.out.println("Returned clones: " + set.size());
        return null;
    }

}
