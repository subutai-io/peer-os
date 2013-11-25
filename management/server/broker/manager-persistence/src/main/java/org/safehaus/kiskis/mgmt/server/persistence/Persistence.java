package org.safehaus.kiskis.mgmt.server.persistence;

import com.datastax.driver.core.*;
import org.apache.cassandra.utils.UUIDGen;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 10:57 PM
 */
public class Persistence implements PersistenceInterface {

    private static final Logger LOG = Logger.getLogger(Persistence.class.getName());
    private Cluster cluster;
    private Session session;
    private long requestsequencenumber = 0l;
    private String cassandraHost;
    private String cassandraKeyspace;
    private int cassandraPort;

    public void setCassandraKeyspace(String cassandraKeyspace) {
        this.cassandraKeyspace = cassandraKeyspace;
    }

    public void setCassandraHost(String cassandraHost) {
        this.cassandraHost = cassandraHost;
    }

    public void setCassandraPort(int cassandraPort) {
        this.cassandraPort = cassandraPort;
    }

    @Override
    public List<Agent> getAgentList() {

        try {
            List<Agent> list = new ArrayList<Agent>();

            ResultSet rs = session.execute("select * from agents");
            for (Object r : rs) {
                Agent agent = new Agent();
                Row row = (Row) r;
                agent.setUuid(row.getString("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                list.add(agent);

                return list;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Saved Agent data into Cassandra agents table
     *
     * @param agent
     * @return the result in boolean
     */
    @Override
    public boolean saveAgent(Agent agent) {
        try {
            String cql = "insert into agents (uuid, hostname, islxc, listip, macaddress, lastheartbeat) "
                    + "values (?,?,?,?,?,?)";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(agent.getUuid(),
                    agent.getHostname(), agent.isIsLXC(), agent.getListIP(),
                    agent.getMacAddress(), new Date()));
            return rs != null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    // TODO Remove this method and reference in blueprint
    public void init() {
        try{
            cluster = Cluster.builder().withPort(cassandraPort).addContactPoint(cassandraHost).build();
            session = cluster.connect(cassandraKeyspace);
            System.out.println("Persistence started");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void destroy() {
        try {
            session.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cluster.shutdown();
            System.out.println(this.getClass().getName() + " stopped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public List<Command> getCommandList(Agent agent) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    /**
     * Saves command into cassandra
     *
     * @param command
     * @return boolean result
     */
    @Override
    public boolean saveCommand(Command command) {
        try{
            String cql = "insert into request (source, requestsequencenumber, type, uuid, taskuuid, "
                    + "workingdirectory, program, outputredirectionstdout, outputredirectionstderr, "
                    + "stdoutpath, erroutpath, runsas, args, environment, pid, timeout) "
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);

            Request request = command.getCommand();
            ResultSet rs = session.execute(boundStatement.bind(request.getSource(), requestsequencenumber++,
                    request.getType() + "", request.getUuid(), request.getTaskUuid(), request.getWorkingDirectory(),
                    request.getProgram(), request.getStdOut() + "", request.getStdErr() + "",
                    request.getStdOutPath(), request.getStdErrPath(), request.getRunAs(), request.getArgs(),
                    request.getEnvironment(), request.getPid(), request.getTimeout()));
            return rs != null;
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return false;
    }

    /**
     * Saves response to cassandra
     *
     * @param response
     * @return boolean result
     */
    @Override
    public boolean saveResponse(Response response) {
        try{
            String cql = "insert into response (uuid, taskuuid, responsesequencenumber, exitcode, errout, hostname, ips, "
                    + "macaddress, pid, requestsequencenumber, responsetype, source, stdout, islxc) "
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(response.getUuid(), response.getTaskUuid(),
                    (Long) response.getResponseSequenceNumber() == null ? -1l : response.getResponseSequenceNumber(),
                    response.getExitCode(), response.getStdErr(), response.getHostname(), response.getIps(),
                    response.getMacAddress(), response.getPid(), response.getRequestSequenceNumber(), response.getType().toString(),
                    response.getSource(), response.getStdOut(), response.isLxc));
            return rs != null;
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public List<Response> getResponses(String taskuuid) {
        try{
            List<Response> list = new ArrayList<Response>();
            ResultSet rs = session.execute("select * from response");
            for (Object r : rs) {
                Response response = new Response();
                Row row = (Row) r;
                response.setType(ResponseType.valueOf(row.getString("responsetype")));
                response.setUuid(row.getString("uuid"));
                response.setExitCode(row.getInt("exitcode"));
                response.setStdOut(row.getString("stdout"));
                list.add(response);
            }
            return list;
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean updateAgent(Agent agent) {
        try{
            String cql = "update agents set hostname = ?, islxc = ?, listip = ?, macaddress = ?, lastheartbeat = ? where uuid = ?";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(
                    agent.getHostname(), agent.isIsLXC(), agent.getListIP(),
                    agent.getMacAddress(), new Date(), agent.getUuid()));
            return rs != null;
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public String saveTask(Task task) {
        try{
            String cql = "insert into tasks (uid, description, status) "
                    + "values (?, ?, ?);";
            PreparedStatement stmt = session.prepare(cql);

            BoundStatement boundStatement = new BoundStatement(stmt);
            UUID uuid = UUIDGen.getTimeUUID();
            session.execute(boundStatement.bind(uuid, task.getDescription(), task.getTaskStatus().toString()));

            return uuid.toString();
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Request> getRequests(String taskuuid) {
        try{
            List<Request> list = new ArrayList<Request>();
            ResultSet rs = session.execute("select * from request");
            for (Object r : rs) {
                Request request = new Request();
                Row row = (Row) r;
                request.setProgram(row.getString("program"));
                list.add(request);
            }
            return list;
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Task> getTasks() {
        List<Task> list = new ArrayList<Task>();
        ResultSet rs = session.execute("select * from tasks");
        Iterator<Row> it = rs.iterator();
        while (it.hasNext()) {
            Row row = it.next();
            Task task = new Task();
            task.setUid(row.getUUID("uid").toString());
            task.setDescription(row.getString("description"));
            task.setTaskStatus(TaskStatus.valueOf(row.getString("status")));
            list.add(task);
        }
        return list;
    }
}
