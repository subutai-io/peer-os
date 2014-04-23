package org.safehaus.kiskis.mgmt.impl.hive;

import java.util.*;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;

public class TaskFactory {

    public static Task checkPackages(Agent agent) {
        Set<Agent> set = new HashSet<Agent>(2);
        set.add(agent);
        return checkPackages(set);
    }

    public static Task checkPackages(Collection<Agent> agents) {
        Task t = new Task("Check installed packages");
        for(Agent agent : agents) {
            t.addRequest(Requests.packageList(), agent);
        }
        return t;
    }

    public static List<Task> installServer(Agent agent, boolean skipHive, boolean skipDerby) {
        List<Task> ls = new ArrayList<Task>();
        if(!skipHive) {
            Task t = new Task("Install Hive on server");
            t.addRequest(Requests.make(Requests.Type.INSTALL, Product.HIVE, 120), agent);
            ls.add(t);
        }
        if(!skipDerby) {
            Task t = new Task("Install Derby on server");
            t.addRequest(Requests.make(Requests.Type.INSTALL, Product.DERBY, 120), agent);
            ls.add(t);
        }

        Task t = new Task("Configure server");
        t.addRequest(Requests.configureHiveServer(agent.getListIP().get(0)), agent);
        ls.add(t);

        return ls;
    }

    public static Task installClient(Collection<Agent> agents) {
        Task t = new Task("Install Hive client(s)");
        for(Agent agent : agents)
            t.addRequest(Requests.make(Requests.Type.INSTALL, Product.HIVE, 120), agent);
        return t;
    }

    public static Task configureClient(Collection<Agent> agents, Agent server) {
        Task t = new Task("Configure Hive client(s)");
        for(Agent agent : agents) {
            String host = "thrift://" + server.getListIP().get(0) + ":10000";
            Request r = Requests.addHivePoperty("add", "hive-site.xml", "hive.metastore.uris", host);
            t.addRequest(r, agent);
        }
        return t;
    }

    public static List<Task> uninstallServer(Agent agent) {
        List<Task> ls = new ArrayList<Task>();

        Task t = new Task("Uninstall Hive from server node");
        t.addRequest(Requests.make(Requests.Type.PURGE, Product.HIVE), agent);
        ls.add(t);

        t = new Task("Uninstall Derby from server node");
        t.addRequest(Requests.make(Requests.Type.PURGE, Product.DERBY), agent);
        ls.add(t);

        return ls;
    }

    public static Task uninstallClient(Collection<Agent> agents) {
        Task t = new Task("Uninstall Hive client(s)");
        for(Agent agent : agents) {
            t.addRequest(Requests.make(Requests.Type.PURGE, Product.HIVE), agent);
        }
        return t;
    }

    public static Task start(Agent agent, Product product) {
        Task t = new Task("Start " + product);
        t.addRequest(Requests.make(Requests.Type.START, product, 60), agent);
        return t;
    }

    public static Task stop(Agent agent, Product product) {
        Task t = new Task("Stop " + product);
        t.addRequest(Requests.make(Requests.Type.STOP, product), agent);
        return t;
    }

    public static Task restart(Agent agent, Product product) {
        Task t = new Task("Restart " + product);
        t.addRequest(Requests.make(Requests.Type.RESTART, product, 90), agent);
        return t;
    }

    public static Task status(Agent agent, Product product) {
        Task t = new Task("Status of " + product);
        t.addRequest(Requests.make(Requests.Type.STATUS, product), agent);
        return t;
    }

}
