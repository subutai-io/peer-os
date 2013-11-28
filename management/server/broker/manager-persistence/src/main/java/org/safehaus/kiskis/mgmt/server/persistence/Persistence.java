package org.safehaus.kiskis.mgmt.server.persistence;

import com.datastax.driver.core.*;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 10:57 PM
 */
public class Persistence implements PersistenceInterface {

    private static final Logger LOG = Logger.getLogger(Persistence.class.getName());
    private Cluster cluster;
    private Session session;
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

    /**
     * Saved Agent data into Cassandra agents table
     *
     * @param agent
     * @return the result in boolean
     */
    @Override
    public boolean saveAgent(Agent agent) {
        if (agent != null) {
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
        }
        return false;
    }

    /**
     * Saved Agent data into Cassandra agents table
     *
     * @param agent
     * @return the result in boolean
     */
//    @Override
//    public boolean removeAgent(Agent agent) {
//        try {
//            if (agent != null) {
//                String cql = "delete from agents where uuid = ?";
//                PreparedStatement stmt = session.prepare(cql);
//                BoundStatement boundStatement = new BoundStatement(stmt);
//                ResultSet rs = session.execute(boundStatement.bind(agent.getUuid()));
//            }
//            return true;
//
//        } catch (Exception ex) {
//            LOG.log(Level.SEVERE, "Error in removeAgent", ex);
//        }
//        return false;
//    }
//
//    @Override
//    public boolean updateAgent(Agent agent) {
//        try {
//            if (agent != null) {
//                String cql =
//                        "update agents set hostname = ?, islxc = ?, listip = ?, macaddress = ?, lastheartbeat = ? where uuid = ?";
//                PreparedStatement stmt = session.prepare(cql);
//                BoundStatement boundStatement = new BoundStatement(stmt);
//                ResultSet rs = session.execute(boundStatement.bind(agent.getHostname(), agent.isIsLXC(), agent.getListIP(), agent.getMacAddress(), new Date(), agent.getUuid()));
//            }
//            return true;
//
//        } catch (Exception ex) {
//            LOG.log(Level.SEVERE, "Error in removeAgent", ex);
//        }
//        return false;
//    }

    @Override
    public Set<Agent> getAgentsByHeartbeat(long from, long to) {
        Set<Agent> list = new HashSet<Agent>();
        try {
            String cql = "select * from agents where islxc = false and lastheartbeat >= ? and lastheartbeat <= ? LIMIT 9999 ALLOW FILTERING";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(
                    new Date(System.currentTimeMillis() - from * 60 * 1000),
                    new Date(System.currentTimeMillis() - to * 60 * 1000)));
            for (Row row : rs) {
                Agent agent = new Agent();
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                list.add(agent);
            }
            cql = "select * from agents where islxc = true and lastheartbeat >= ? and lastheartbeat <= ? LIMIT 9999 ALLOW FILTERING";
            stmt = session.prepare(cql);
            boundStatement = new BoundStatement(stmt);
            rs = session.execute(boundStatement.bind(
                    new Date(System.currentTimeMillis() - from * 60 * 1000),
                    new Date(System.currentTimeMillis() - to * 60 * 1000)));
            for (Row row : rs) {
                Agent agent = new Agent();
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                list.add(agent);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getAgentsByHeartbeat", ex);
        }
        return list;
    }

    @Override
    public Set<Agent> getRegisteredAgents(long freshness) {
        Set<Agent> list = new HashSet<Agent>();
        list.addAll(getRegisteredPhysicalAgents(freshness));
        list.addAll(getRegisteredLxcAgents(freshness));
        return list;
    }

    @Override
    public Set<Agent> getRegisteredLxcAgents(long freshness) {
        Set<Agent> list = new HashSet<Agent>();
        try {
            String cql = "select * from agents where islxc = true and lastheartbeat >= ? LIMIT 9999 ALLOW FILTERING";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(
                    new Date(System.currentTimeMillis() - freshness * 60 * 1000)));
            for (Row row : rs) {
                Agent agent = new Agent();
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                list.add(agent);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRegisteredAgents", ex);
        }
        return list;
    }

    @Override
    public Set<Agent> getRegisteredPhysicalAgents(long freshness) {
        Set<Agent> list = new HashSet<Agent>();
        try {
            String cql = "select * from agents where islxc = false and lastheartbeat >= ? LIMIT 9999 ALLOW FILTERING";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(
                    new Date(System.currentTimeMillis() - freshness * 60 * 1000)));
            for (Row row : rs) {
                Agent agent = new Agent();
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                list.add(agent);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRegisteredAgents", ex);
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

    /**
     * Saves command into cassandra
     *
     * @param command
     * @return boolean result
     */
    @Override
    public boolean saveCommand(Command command) {
        try {
            String cql = "insert into requests (source, reqseqnum, type, agentuuid, taskuuid, "
                    + "workingdirectory, program, outputredirectionstdout, outputredirectionstderr, "
                    + "stdoutpath, erroutpath, runsas, args, environment, pid, timeout) "
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);

            Request request = command.getCommand();
            ResultSet rs = session.execute(boundStatement.bind(request.getSource(), request.getRequestSequenceNumber(),
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

            String cql = "insert into responses (agentuuid, taskuuid, resseqnum, exitcode, errout, hostname, ips, "
                    + "macaddress, pid, reqseqnum, responsetype, source, stdout, islxc) "
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(response.getUuid(), response.getTaskUuid(),
                    response.getResponseSequenceNumber() == null ? -1 : response.getResponseSequenceNumber(),
                    response.getExitCode(), response.getStdErr(), response.getHostname(), response.getIps(),
                    response.getMacAddress(), response.getPid(),
                    response.getRequestSequenceNumber() == null ? -1 : response.getRequestSequenceNumber(),
                    response.getType().toString(),
                    response.getSource(), response.getStdOut(), response.isLxc));
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveResponse", ex);
        }
        return false;
    }

    @Override
    public List<Response> getResponses(UUID taskuuid, Integer requestSequenceNumber) {
        List<Response> list = new ArrayList<Response>();
        try {
            String cql = "select * from responses "
                    + "WHERE taskuuid = ? "
                    + "and reqseqnum = ? and resseqnum >= 0 "
                    + "ORDER BY reqseqnum, resseqnum";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(taskuuid, requestSequenceNumber));
            for (Row row : rs) {
                Response response = new Response();
                response.setType(ResponseType.valueOf(row.getString("responsetype")));
                response.setUuid(row.getUUID("agentuuid"));
                response.setExitCode(row.getInt("exitcode"));
                response.setStdOut(row.getString("stdout"));
                response.setHostname(row.getString("hostname"));
                response.setIps(row.getList("ips", String.class));
                response.setMacAddress(row.getString("macaddress"));
                response.setPid(row.getInt("pid"));
                response.setRequestSequenceNumber(row.getInt("reqseqnum"));
                response.setResponseSequenceNumber(row.getInt("resseqnum"));
                response.setSource(row.getString("source"));
                response.setStdErr(row.getString("errout"));
                response.setStdOut(row.getString("stdout"));
                response.setTaskUuid(row.getUUID("taskuuid"));
                response.setType(ResponseType.valueOf(row.getString("responsetype")));
                list.add(response);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getResponses", ex);
        }
        return list;
    }

    @Override
    public String saveTask(Task task) {
        try {
            String cql = "insert into tasks (uuid, description, status) "
                    + "values (?, ?, ?);";
            PreparedStatement stmt = session.prepare(cql);

            BoundStatement boundStatement = new BoundStatement(stmt);

            session.execute(boundStatement.bind(task.getUuid(), task.getDescription(), task.getTaskStatus().toString()));

            return task.getUuid().toString();

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveTask", ex);
        }
        return null;
    }

    @Override
    public List<Request> getRequests(UUID taskuuid) {
        List<Request> list = new ArrayList<Request>();
        try {
            ResultSet rs = session.execute("select * from requests");
            for (Row row : rs) {
                Request request = new Request();
                request.setProgram(row.getString("program"));
                request.setArgs(row.getList("args", String.class));
                request.setEnvironment(row.getMap("environment", String.class, String.class));
                request.setPid(row.getInt("pid"));
                request.setProgram(row.getString("program"));
                request.setRequestSequenceNumber(row.getInt("reqseqnum"));
                request.setRunAs(row.getString("runsas"));
                request.setSource(row.getString("source"));
                request.setStdErr(OutputRedirection.valueOf(row.getString("outputredirectionstderr")));
                request.setStdErrPath(row.getString("erroutpath"));
                request.setStdOut(OutputRedirection.valueOf(row.getString("outputredirectionstdout")));
                request.setStdOutPath(row.getString("stdoutpath"));
                request.setTaskUuid(row.getUUID("taskuuid"));
                request.setTimeout(row.getInt("timeout"));
                request.setType(RequestType.valueOf(row.getString("type")));
                request.setUuid(row.getUUID("agentuuid"));
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
                task.setUuid(row.getUUID("uuid"));
                task.setDescription(row.getString("description"));
                task.setTaskStatus(TaskStatus.valueOf(row.getString("status")));
                list.add(task);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getTasks", ex);
        }
        return list;
    }

    public boolean truncateTables() {
        try {
            session.execute("truncate agents");
            session.execute("truncate tasks");
            session.execute("truncate requests");
            session.execute("truncate responses");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getTasks", ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean saveClusterData(ClusterData cluster) {
        try {
            String cql = "insert into clusterdata (uid, name, commitlogdir, datadir, "
                    + "nodes, savedcachedir, seeds) "
                    + "values (?,?,?,?,?,?,?)";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(cluster.getUuid(), cluster.getName(),
                    cluster.getCommitLogDir(), cluster.getDataDir(), cluster.getNodes(),
                    cluster.getSavedCacheDir(), cluster.getSeeds()));

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveAgent", ex);
            return false;
        }
        return true;
    }
}
