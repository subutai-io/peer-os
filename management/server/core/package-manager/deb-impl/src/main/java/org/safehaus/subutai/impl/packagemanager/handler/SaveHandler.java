package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.DebPackageManager;
import org.safehaus.subutai.shared.protocol.Agent;

public class SaveHandler extends AbstractHandler<Collection<PackageInfo>> {

    private final String hostname;

    public SaveHandler(DebPackageManager pm, String hostname) {
        super(pm);
        this.hostname = hostname;
    }

    @Override
    public Collection<PackageInfo> performAction() {
        Agent a = packageManager.getAgentManager().getAgentByHostname(hostname);
        if(a != null) {
            RequestBuilder rb = new RequestBuilder(
                    "dpkg -l > " + packageManager.getFilename())
                    .withCwd(packageManager.getLocation());
            Command cmd = packageManager.getCommandRunner().createCommand(rb,
                    new HashSet<>(Arrays.asList(a)));
            packageManager.getCommandRunner().runCommand(cmd);
            if(cmd.hasSucceeded()) {
                ListHandler lh = new ListHandler(packageManager, hostname);
                lh.setFromFile(true);
                return lh.performAction();
            }
        }
        return null;
    }

}
