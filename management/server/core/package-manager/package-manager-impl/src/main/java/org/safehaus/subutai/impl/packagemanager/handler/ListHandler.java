package org.safehaus.subutai.impl.packagemanager.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.api.packagemanager.PackageFlag;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.api.packagemanager.PackageState;
import org.safehaus.subutai.api.packagemanager.SelectionState;
import org.safehaus.subutai.impl.packagemanager.PackageManagerImpl;
import org.safehaus.subutai.shared.protocol.Agent;

public class ListHandler extends AbstractHandler<Collection<PackageInfo>> {

    private final String hostname;

    public ListHandler(PackageManagerImpl pm, String hostname) {
        super(pm);
        this.hostname = hostname;
    }

    @Override
    public Collection<PackageInfo> performAction() {
        Agent agent = pm.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) return Collections.emptyList();

        String s = "dpkg-query -W -f='${db:Status-Abbrev}\t${binary:Package}\t${Version}\t${binary:Summary}\n'";
        AgentRequestBuilder rb = new AgentRequestBuilder(agent, s);
        Command cmd = pm.getCommandRunner().createCommand(new HashSet<>(Arrays.asList(rb)));
        pm.getCommandRunner().runCommand(cmd);
        if(cmd.hasSucceeded()) {
            Collection<PackageInfo> ls = new ArrayList<>();
            AgentResult res = cmd.getResults().get(agent.getUuid());
            s = res.getStdOut();
            try(BufferedReader br = new BufferedReader(new StringReader(s))) {
                while((s = br.readLine()) != null) {
                    PackageInfo pi = parseLine(s);
                    ls.add(pi);
                }
            } catch(IOException ex) {
                // TODO:
            }
            return ls;
        }
        return Collections.emptyList();
    }

    PackageInfo parseLine(String s) {
        String[] arr = s.split("\\t");
        char[] status = arr[0].toCharArray();
        PackageInfo p = new PackageInfo(arr[1], arr[2]);
        p.setDescription(arr[3]);
        p.setSelectionState(SelectionState.getByAbbrev(status[0]));
        p.setState(PackageState.getByAbbrev(status[1]));
        if(status.length > 2) p.getFlags().add(PackageFlag.getByAbbrev(status[2]));
        return p;
    }

}
