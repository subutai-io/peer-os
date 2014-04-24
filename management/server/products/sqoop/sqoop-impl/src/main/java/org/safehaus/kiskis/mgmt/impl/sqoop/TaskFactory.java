package org.safehaus.kiskis.mgmt.impl.sqoop;

import java.util.*;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class TaskFactory {

    public static Task checkPackages(Agent agent) {
        return checkPackages(Arrays.asList(agent));
    }

    public static Task checkPackages(Collection<Agent> agents) {
        Task t = new Task("Check installed packages");
        for(Agent agent : agents) {
            t.addRequest(Requests.packageList(), agent);
        }
        return t;
    }

    public static Task install(Agent agent) {
        return install(Arrays.asList(agent));
    }

    public static Task install(Collection<Agent> agents) {
        Task t = new Task("Install Sqoop");
        for(Agent agent : agents)
            t.addRequest(Requests.make(Requests.Type.INSTALL, 120), agent);
        return t;
    }

    public static Task uninstall(Agent agent) {
        return uninstall(Arrays.asList(agent));
    }

    public static Task uninstall(Collection<Agent> agents) {
        Task t = new Task("Uninstall Sqoop");
        for(Agent agent : agents) {
            t.addRequest(Requests.make(Requests.Type.PURGE), agent);
        }
        return t;
    }

}
