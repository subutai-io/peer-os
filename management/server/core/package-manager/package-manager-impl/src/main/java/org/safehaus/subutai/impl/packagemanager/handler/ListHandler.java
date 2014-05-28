package org.safehaus.subutai.impl.packagemanager.handler;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.api.packagemanager.*;
import org.safehaus.subutai.impl.packagemanager.PackageManagerImpl;
import org.safehaus.subutai.shared.protocol.Agent;

public class ListHandler extends AbstractHandler<Collection<PackageInfo>> {

    private final String hostname;
    private Pattern lineStartPattern = Pattern.compile("[a-z]{2,3}\\s+");

    public ListHandler(PackageManagerImpl pm, String hostname) {
        super(pm);
        this.hostname = hostname;
    }

    @Override
    public Collection<PackageInfo> performAction() {
        Agent agent = pm.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) return Collections.emptyList();

        AgentRequestBuilder rb = new AgentRequestBuilder(agent, "dpkg -l");
        Command cmd = pm.getCommandRunner().createCommand(new HashSet<>(Arrays.asList(rb)));
        pm.getCommandRunner().runCommand(cmd);
        if(cmd.hasSucceeded()) {
            Collection<PackageInfo> ls = new ArrayList<>();
            AgentResult res = cmd.getResults().get(agent.getUuid());
            String s = res.getStdOut();
            Pattern delim = Pattern.compile("\\s+");
            try(BufferedReader br = new BufferedReader(new StringReader(s))) {
                while((s = br.readLine()) != null) {
                    PackageInfo pi = parseLine(s, delim);
                    if(pi != null) ls.add(pi);
                }
            } catch(IOException ex) {
                // TODO:
            }
            return ls;
        }
        return Collections.emptyList();
    }

    PackageInfo parseLine(String s, Pattern delim) {
        if(!lineStartPattern.matcher(s).matches()) return null;
        String[] arr = delim.split(s);
        PackageInfo p = null;
        if(arr.length > 3) {
            char[] status = arr[0].toCharArray();
            p = new PackageInfo(arr[1], arr[2]);
            p.setDescription(arr[3]);
            p.setSelectionState(SelectionState.getByAbbrev(status[0]));
            p.setState(PackageState.getByAbbrev(status[1]));
            if(status.length > 2) {
                PackageFlag f = PackageFlag.getByAbbrev(status[2]);
                if(f != null) p.getFlags().add(f);
            }
        }
        return p;
    }

}
