package org.safehaus.subutai.impl.manager;

import java.util.*;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.shared.protocol.Agent;

public class TemplateManagerImpl extends TemplateManagerBase {

    @Override
    public String getMasterTemplateName() {
        return "master";
    }

    @Override
    public boolean clone(String hostName, String templateName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.CLONE, templateName, cloneName);
    }

    @Override
    public boolean cloneDestroy(String hostName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.CLONE_DESTROY, cloneName);
    }

    @Override
    public boolean promoteClone(String hostName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.TEMPLATE, cloneName);
    }

    @Override
    public boolean importTemplate(String hostName, String templateName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        if(checkParentTemplate(a, templateName))
            return scriptExecutor.execute(a, ActionType.IMPORT, templateName);
        return false;
    }

    @Override
    public boolean exportTemplate(String hostName, String templateName) {
        // check if registered as template
        Template template = templateRegistry.getTemplate(templateName);
        if(template == null) return false;

        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.EXPORT, templateName);
    }

    private boolean checkParentTemplate(Agent a, String templateName) {
        Template parent = templateRegistry.getParentTemplate(templateName);
        String parentName = parent.getTemplateName();

        if(parentName.equals(getMasterTemplateName())) return true;

        if(!zfsIsTemplate(a, parentName)) {
            boolean b = scriptExecutor.execute(a, ActionType.IMPORT, parentName);
            if(!b) return false;
        }
        return checkParentTemplate(a, parentName);
    }

    private boolean zfsIsTemplate(Agent a, String containerName) {
        String s = String.format(
                "zfs list -t snapshot -o name -H | grep \"lxc/%s@template\"",
                containerName);
        Command cmd = commandRunner.createCommand(new RequestBuilder(s),
                new HashSet<>(Arrays.asList(a)));
        commandRunner.runCommand(cmd);

        // exit status of grep is 0 if selected lines are found
        return cmd.hasSucceeded();
    }

}
