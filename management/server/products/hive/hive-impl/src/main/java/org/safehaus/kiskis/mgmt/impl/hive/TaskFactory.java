package org.safehaus.kiskis.mgmt.impl.hive;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class TaskFactory {

    public static Task checkPackages(Agent agent) {
        Set<Agent> set = new HashSet<Agent>(2);
        set.add(agent);
        return checkPackages(set);
    }

    public static Task checkPackages(Set<Agent> agents) {
        Task t = new Task("Check installed packages");
        for(Agent agent : agents) {
            t.addRequest(Requests.packageList(), agent);
        }
        return t;
    }

    public static Task installServer(Agent agent, boolean skipHive, boolean skipDerby) {
        Task t = new Task("Install Hive server");
        if(!skipHive)
            t.addRequest(Requests.make(Requests.Type.INSTALL, Product.HIVE, 120), agent);
        if(!skipDerby)
            t.addRequest(Requests.make(Requests.Type.INSTALL, Product.DERBY, 120), agent);
        t.addRequest(Requests.configureServer(agent.getListIP().get(0)), agent);
        return t;
    }

    public static Task installClient(Set<Agent> agents) {
        return installClient(agents, false);
    }

    public static Task installClient(Set<Agent> agents, boolean skipInstall) {
        Task t = new Task("Install Hive client");
        for(Agent agent : agents) {
            if(!skipInstall)
                t.addRequest(Requests.make(Requests.Type.INSTALL, Product.HIVE, 120), agent);
            String host = "thrift://" + agent.getListIP().get(0) + ":10000";
            t.addRequest(Requests.addPoperty("add", "hive-site.xml", "hive.metastore.uris", host), agent);
        }
        return t;
    }

    public static Task uninstallServer(Agent agent) {
        Task t = stop(agent);
        t.setDescription("Uninstall Hive server");
        t.addRequest(Requests.make(Requests.Type.STOP, Product.DERBY), agent);
        t.addRequest(Requests.make(Requests.Type.STOP, Product.HIVE), agent);
        t.addRequest(Requests.make(Requests.Type.PURGE, Product.DERBY), agent);
        t.addRequest(Requests.make(Requests.Type.PURGE, Product.HIVE), agent);
        return t;
    }

    public static Task uninstallClient(Set<Agent> agents) {
        Task t = new Task("Uninstall Hive client(s)");
        for(Agent agent : agents) {
            t.addRequest(Requests.make(Requests.Type.STOP, Product.HIVE), agent);
            t.addRequest(Requests.make(Requests.Type.PURGE, Product.HIVE), agent);
        }
        return t;
    }

    public static Task start(Agent agent) {
        Task t = new Task("Start Hive");
        t.addRequest(Requests.make(Requests.Type.START, Product.DERBY, 60), agent);
        t.addRequest(Requests.make(Requests.Type.START, Product.HIVE, 60), agent);
        return t;
    }

    public static Task stop(Agent agent) {
        Task t = new Task("Stop Hive");
        t.addRequest(Requests.make(Requests.Type.STOP, Product.HIVE), agent);
        t.addRequest(Requests.make(Requests.Type.STOP, Product.DERBY), agent);
        return t;
    }

    public static Task restart(Agent agent) {
        Task t = new Task("Restart Hive");
        t.addRequest(Requests.make(Requests.Type.RESTART, Product.HIVE, 90), agent);
        t.addRequest(Requests.make(Requests.Type.RESTART, Product.DERBY, 90), agent);
        return t;
    }

    public static Task status(Agent agent) {
        Task t = new Task("Status of Hive");
        t.addRequest(Requests.make(Requests.Type.STATUS, Product.HIVE), agent);
        t.addRequest(Requests.make(Requests.Type.STATUS, Product.DERBY), agent);
        return t;
    }
}
