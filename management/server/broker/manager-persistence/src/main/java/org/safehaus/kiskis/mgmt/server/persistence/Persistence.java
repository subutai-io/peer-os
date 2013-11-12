package org.safehaus.kiskis.mgmt.server.persistence;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceAgentInterface;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceCommandInterface;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 10:57 PM
 */
public class Persistence implements PersistenceAgentInterface, PersistenceCommandInterface {

    private static final Logger LOG = Logger.getLogger(Persistence.class.getName());

    private Cluster cluster;
    private Session session;
    private final int cassandraPort = 9042;
//    private final String cassandraHost = "localhost";
    private final String cassandraHost = "192.168.1.106";
    private final String keyspaceName = "kiskis";
    private long requestsequencenumber = 0l;

    public Persistence() {
        try {
            System.out.println("Consctructor initialized...");
            cluster = Cluster.builder().withPort(cassandraPort).addContactPoint(cassandraHost).build();
            session = cluster.connect(keyspaceName);
        } catch (Exception e) {
            System.out.println("can not connect to cassandra.");
        }
    }

    @Override
    public List<Agent> getAgentList() {
        System.out.println(this.getClass().getName() + " getAgentList called");
        return null;
    }

    @Override
    public boolean saveAgent(Agent agent) {
        System.out.println(this.getClass().getName() + " saveAgent called");
        String cql = "insert into agents (uuid, hostname) values ('" + agent.getUuid() + "', 'a" + agent.getHostname() + "')";
        System.out.println(cql);
        ResultSet rs = session.execute(cql);
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
        session.shutdown();
        cluster.shutdown();
        System.out.println(this.getClass().getName() + " stopped");
    }

    public List<Command> getCommandList(Agent agent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean saveCommand(Command command) {
        System.out.println(this.getClass().getName() + " saveCommand called");
        PreparedStatement stmt = session.prepare("insert into request (uuid, program, requestsequencenumber) "
                + "values (?, ?, ?);");
        BoundStatement boundStatement = new BoundStatement(stmt);
        Request request = command.getCommand();
        ResultSet rs = session.execute(boundStatement.bind(request.getUuid(), request.getProgram(),
                requestsequencenumber++));
        if (rs != null) {
            System.out.println("request saved into cassandra " + request.toString());
            return true;
        }
        return false;
    }

    @Override
    public boolean saveResponse(Response response) {
        System.out.println(this.getClass().getName() + " saveResponse called");
        System.out.println(response.toString());
        PreparedStatement stmt = session.prepare("insert into response (uuid, stdout, responsesequencenumber) "
                + "values (?, ?, ?);");
        BoundStatement boundStatement = new BoundStatement(stmt);
        ResultSet rs = session.execute(boundStatement.bind(response.getUuid(), "stdout" + response.getStdOut(),
                (Long) response.getResponseSequenceNumber() == null ? -1l : response.getResponseSequenceNumber()));
        if (rs != null) {
            System.out.println("response saved into cassandra " + response.toString());
            return true;
        }
        return false;
    }
}
