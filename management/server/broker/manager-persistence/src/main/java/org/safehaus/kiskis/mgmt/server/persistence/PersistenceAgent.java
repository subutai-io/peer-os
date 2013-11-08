package org.safehaus.kiskis.mgmt.server.persistence;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceAgentInterface;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 10:57 PM
 */
public class PersistenceAgent implements PersistenceAgentInterface {

    private static final Logger LOG = Logger.getLogger(PersistenceAgent.class.getName());

    private Cluster cluster;
    private Session session;
    private final int cassandraPort = 9042;
    private final String cassandraHost = "localhost";
    private final String keyspaceName = "kiskis";

    public PersistenceAgent() {
        System.out.println("Consctructor initialized...");
        cluster = Cluster.builder().withPort(cassandraPort).addContactPoint(cassandraHost).build();
        session = cluster.connect(keyspaceName);
    }

    @Override
    public List<Agent> getAgentList() {
        System.out.println(this.getClass().getName() + " getAgentList called");
        return null;
    }

    @Override
    public boolean saveAgent(Agent agent) {
        System.out.println(this.getClass().getName() + " saveAgent called");
        ResultSet rs = session.execute("insert into users (key, full_name) values  ('kiskis', 'bahadyr')");
        if (rs != null) {
            System.out.println("saved into cassandra " + agent.toString());
            return true;
        }
        return false;
    }

    // TODO Remove this method and reference in blueprint
    public void init() {
        System.out.println(this.getClass().getName() + " started");
        LOG.log(Level.INFO, "{0}initializing", this.getClass().getName());
    }

    public void destroy() {
//        session.shutdown();
//        cluster.shutdown();
        System.out.println(this.getClass().getName() + " stopped");
    }
}
