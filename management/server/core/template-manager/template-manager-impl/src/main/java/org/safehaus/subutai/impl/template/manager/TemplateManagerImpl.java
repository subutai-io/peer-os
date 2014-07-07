package org.safehaus.subutai.impl.template.manager;

import java.nio.file.Paths;
import java.util.*;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepoException;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.shared.protocol.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateManagerImpl extends TemplateManagerBase {

    private static final Logger logger = LoggerFactory.getLogger(TemplateManagerImpl.class);

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
        boolean b = scriptExecutor.execute(a, ActionType.EXPORT, templateName);
        if(b) {
            String filePath = getExportedPackageFilePath(a, templateName);
            try {
                repoManager.addPackageByPath(a, filePath, false);
                return true;
            } catch(AptRepoException ex) {
                logger.error("Failed to add package to repo", ex);
            }
        }
        return false;
    }

    private boolean checkParentTemplate(Agent a, String templateName) {
        Template parent = templateRegistry.getParentTemplate(templateName);
        if(parent == null) {
            logger.error("Parent not defined for template {}", templateName);
            return false;
        }

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

    private String getExportedPackageFilePath(Agent a, String templateName) {
        Command cmd = commandRunner.createCommand(
                new RequestBuilder("echo $SUBUTAI_TMPDIR"),
                new HashSet<>(Arrays.asList(a)));
        commandRunner.runCommand(cmd);
        AgentResult res = cmd.getResults().get(a.getUuid());
        if(res.getExitCode() != null && res.getExitCode() == 0) {
            String dir = res.getStdOut();
            return Paths.get(dir, templateName).toString();
        }
        return null;
    }

}
