package org.safehaus.kiskis.mgmt.server.persistence;

import com.datastax.driver.core.*;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

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

//    @Override
//    public List<Agent> getAgentList() {
//        List<Agent> list = new ArrayList<Agent>();
//        try {
//            ResultSet rs = session.execute("select * from agents");
//            for (Row row : rs) {
//                Agent agent = new Agent();
//                agent.setUuid(row.getString("uuid"));
//                agent.setHostname(row.getString("hostname"));
//                agent.setIsLXC(row.getBool("islxc"));
//                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
//                agent.setListIP(row.getList("listip", String.class));
//                agent.setMacAddress(row.getString("macaddress"));
//                list.add(agent);
//            }
//        } catch (Exception ex) {
//            LOG.log(Level.SEVERE, "Error in getAgentList", ex);
//        }
//        return list;
//    }
    @Override
    public Set<Agent> getAgentsByHeartbeat(long from, long to) {
        Set<Agent> list = new HashSet<Agent>();
        try {
            String cql = "select * from agents where lastheartbeat >= ? and lastheartbeat <= ? LIMIT 9999 ALLOW FILTERING";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(
                    new Date(System.currentTimeMillis() - from * 60 * 1000),
                    new Date(System.currentTimeMillis() - to * 60 * 1000)));
            for (Row row : rs) {
                Agent agent = new Agent();
                agent.setUuid(row.getString("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                list.add(agent);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getAgentList", ex);
        }
        return list;
    }

    @Override
    public Set<Agent> getRegisteredAgents(long freshness) {
        Set<Agent> list = new HashSet<Agent>();
        try {
            String cql = "select * from agents where lastheartbeat >= ? LIMIT 9999 ALLOW FILTERING";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(
                    new Date(System.currentTimeMillis() - freshness * 60 * 1000)));
            for (Row row : rs) {
                Agent agent = new Agent();
                agent.setUuid(row.getString("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                list.add(agent);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getAgentList", ex);
        }
        return list;
    }

    // TODO Remove this method and reference in blueprint
    public void init() {
        try {
            cluster = Cluster.builder().withPort(cassandraPort).addContactPoint(cassandraHost).build();
            session = cluster.connect(cassandraKeyspace);
            System.out.println("Persistence started");

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in init", ex);
        }
    }

    public void destroy() {
        try {
            session.shutdown();
        } catch (Exception e) {
        }
        try {
            cluster.shutdown();
        } catch (Exception e) {
        }
        System.out.println(this.getClass().getName() + " stopped");
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
        try {
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
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveCommand", ex);
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
        try {

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
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveResponse", ex);
        }
        return false;
    }

    @Override
    public List<Response> getResponses(String taskuuid) {
        List<Response> list = new ArrayList<Response>();
        try {
            ResultSet rs = session.execute("select * from response");
            for (Row row : rs) {
                Response response = new Response();
                response.setType(ResponseType.valueOf(row.getString("responsetype")));
                response.setUuid(row.getString("uuid"));
                response.setExitCode(row.getInt("exitcode"));
                response.setStdOut(row.getString("stdout"));
                response.setHostname(row.getString("hostname"));
                response.setIps(row.getList("ips", String.class));
                response.setMacAddress(row.getString("macaddress"));
                response.setPid(row.getInt("pid"));
                response.setRequestSequenceNumber(row.getLong("requestsequencenumber"));
                response.setResponseSequenceNumber(row.getLong("responsesequencenumber"));
                response.setSource(row.getString("source"));
                response.setStdErr(row.getString("errout"));
                response.setStdOut(row.getString("stdout"));
                response.setTaskUuid(row.getString("taskuuid"));
                response.setType(ResponseType.valueOf(row.getString("responsetype")));
                list.add(response);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getResponses", ex);
        }
        return list;
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
            return true;

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveAgent", ex);
        }
        return false;
    }

    /**
     * Saved Agent data into Cassandra agents table
     *
     * @param agent
     * @return the result in boolean
     */
    @Override
    public boolean removeAgent(Agent agent) {
        try {
            String cql = "delete from agents where uuid = ?";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(agent.getUuid()));
            return true;

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in removeAgent", ex);
        }
        return false;
    }

    @Override
    public boolean updateAgent(Agent agent) {
        if (removeAgent(agent)) {
            return saveAgent(agent);
        }
        return false;
    }

    @Override
    public String saveTask(Task task) {
        try {
            String cql = "insert into tasks (uid, description, status) "
                    + "values (?, ?, ?);";
            PreparedStatement stmt = session.prepare(cql);

            BoundStatement boundStatement = new BoundStatement(stmt);
            UUID uuid = Persistence.getTimeUUID();
            session.execute(boundStatement.bind(uuid, task.getDescription(), task.getTaskStatus().toString()));

            return uuid.toString();

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveTask", ex);
        }
        return null;
    }

    @Override
    public List<Request> getRequests(String taskuuid) {
        List<Request> list = new ArrayList<Request>();
        try {
            ResultSet rs = session.execute("select * from request");
            for (Row row : rs) {
                Request request = new Request();
                request.setProgram(row.getString("program"));
                request.setArgs(row.getList("args", String.class));
                request.setEnvironment(row.getMap("environment", String.class, String.class));
                request.setPid(row.getInt("pid"));
                request.setProgram(row.getString("program"));
                request.setRequestSequenceNumber(row.getLong("requestsequencenumber"));
                request.setRunAs(row.getString("runsas"));
                request.setSource(row.getString("source"));
                request.setStdErr(OutputRedirection.valueOf(row.getString("outputredirectionstderr")));
                request.setStdErrPath(row.getString("erroutpath"));
                request.setStdOut(OutputRedirection.valueOf(row.getString("outputredirectionstdout")));
                request.setStdOutPath(row.getString("stdoutpath"));
                request.setTaskUuid(row.getString("taskuuid"));
                request.setTimeout(row.getLong("timeout"));
                request.setType(RequestType.valueOf(row.getString("type")));
                request.setUuid(row.getString("uuid"));
                request.setWorkingDirectory(row.getString("workingdirectory"));
                list.add(request);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRequests", ex);
        }
        return list;
    }

    @Override
    public List<Task> getTasks() {
        List<Task> list = new ArrayList<Task>();
        try {
            ResultSet rs = session.execute("select * from tasks");
            for (Row row : rs) {
                Task task = new Task();
                task.setUid(row.getUUID("uid").toString());
                task.setDescription(row.getString("description"));
                task.setTaskStatus(TaskStatus.valueOf(row.getString("status")));
                list.add(task);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getTasks", ex);
        }
        return list;
    }

    public static java.util.UUID getTimeUUID() {
        return java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
    }

    public boolean truncateTables() {
        try {
            session.execute("truncate tasks");
            session.execute("truncate request");
            session.execute("truncate response");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getTasks", ex);
            return false;
        }
        return true;
    }
}
