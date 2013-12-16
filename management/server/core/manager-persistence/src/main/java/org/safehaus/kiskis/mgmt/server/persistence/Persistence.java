package org.safehaus.kiskis.mgmt.server.persistence;

import com.datastax.driver.core.*;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
                String cql = "insert into agents (uuid, hostname, islxc, listip, macaddress, lastheartbeat,parenthostname) "
                        + "values (?,?,?,?,?,?,?)";
                PreparedStatement stmt = session.prepare(cql);
                BoundStatement boundStatement = new BoundStatement(stmt);

                ResultSet rs = session.execute(boundStatement.bind(agent.getUuid(),
                        agent.getHostname(), agent.isIsLXC(), agent.getListIP(),
                        agent.getMacAddress(), new Date(), agent.getParentHostName()));
                return true;

            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in saveAgent", ex);
            }
        }
        return false;
    }

    @Override
    public List<Agent> getAgentsByHeartbeat(long from, long to) {
        List<Agent> list = new ArrayList<Agent>();
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
                agent.setParentHostName(row.getString("parenthostname"));
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
                agent.setParentHostName(row.getString("parenthostname"));
                list.add(agent);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getAgentsByHeartbeat", ex);
        }
        return list;
    }

    @Override
    public List<Agent> getRegisteredAgents(long freshness) {
        List<Agent> list = new ArrayList<Agent>();
        list.addAll(getRegisteredPhysicalAgents(freshness));
        list.addAll(getRegisteredLxcAgents(freshness));
        return list;
    }

    @Override
    public List<Agent> getRegisteredLxcAgents(long freshness) {
        List<Agent> list = new ArrayList<Agent>();
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
                agent.setParentHostName(row.getString("parenthostname"));
                list.add(agent);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRegisteredLxcAgents", ex);
        }
        return list;
    }

    @Override
    public List<Agent> getRegisteredPhysicalAgents(long freshness) {
        List<Agent> list = new ArrayList<Agent>();
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
                agent.setParentHostName(row.getString("parenthostname"));
                list.add(agent);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRegisteredPhysicalAgents", ex);
        }
        return list;
    }

    @Override
    public List<Agent> getRegisteredChildLxcAgents(Agent parent, long freshness) {
        List<Agent> list = new ArrayList<Agent>();
        try {
            String cql = "select * from agents where islxc = true and parenthostname = ? and lastheartbeat >= ? LIMIT 9999 ALLOW FILTERING";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(parent.getHostname(),
                    new Date(System.currentTimeMillis() - freshness * 60 * 1000)));
            for (Row row : rs) {
                Agent agent = new Agent();
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                agent.setParentHostName(row.getString("parenthostname"));
                list.add(agent);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRegisteredChildLxcAgents", ex);
        }
        return list;
    }

    @Override
    public List<Agent> getUnknownChildLxcAgents(long freshness) {
        List<Agent> list = new ArrayList<Agent>();
        try {
            String cql = "select * from agents where islxc = true and parenthostname = ? and lastheartbeat >= ? LIMIT 9999 ALLOW FILTERING";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(Common.UNKNOWN_LXC_PARENT_NAME,
                    new Date(System.currentTimeMillis() - freshness * 60 * 1000)));
            for (Row row : rs) {
                Agent agent = new Agent();
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                agent.setParentHostName(row.getString("parenthostname"));
                list.add(agent);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getUnknownChildLxcAgents", ex);
        }
        return list;
    }

    @Override
    public Agent getRegisteredLxcAgentByHostname(String hostname, long freshness) {
        Agent agent = null;
        try {
            String cql = "select * from agents where islxc = true and hostname = ? LIMIT 1 ALLOW FILTERING";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(Common.UNKNOWN_LXC_PARENT_NAME,
                    new Date(System.currentTimeMillis() - freshness * 60 * 1000)));
            Row row = rs.one();
            if (row != null) {
                agent = new Agent();
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                agent.setParentHostName(row.getString("parenthostname"));
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRegisteredLxcAgentByHostname", ex);
        }
        return agent;
    }

    @Override
    public Agent getRegisteredPhysicalAgentByHostname(String hostname, long freshness) {
        Agent agent = null;
        try {
            String cql = "select * from agents where islxc = false and hostname = ? LIMIT 1 ALLOW FILTERING";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(Common.UNKNOWN_LXC_PARENT_NAME,
                    new Date(System.currentTimeMillis() - freshness * 60 * 1000)));
            Row row = rs.one();
            if (row != null) {
                agent = new Agent();
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                agent.setParentHostName(row.getString("parenthostname"));
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRegisteredPhysicalAgentByHostname", ex);
        }
        return agent;
    }

    @Override
    public Agent getAgent(UUID uuid) {
        Agent agent = new Agent();
        try {
            String cql = "select * from agents where uuid = ?";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(uuid));
            for (Row row : rs) {
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                agent.setParentHostName(row.getString("parenthostname"));
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getAgent", ex);
        }
        return agent;
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
        System.out.println("Persistence stopped");
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
                    response.getExitCode() == null ? -1 : response.getExitCode(), response.getStdErr(),
                    response.getHostname(), response.getIps(),
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
    public Integer getResponsesCount(UUID taskUuid) {
        Integer count = 0;
        try {
            String cql = "select * from responses where taskuuid = ?;";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(taskUuid));
            for (Row row : rs) {
                String type = row.getString("responsetype");
                if (ResponseType.EXECUTE_RESPONSE_DONE.equals(ResponseType.valueOf(type))
                        || ResponseType.EXECUTE_TIMEOUTED.equals(ResponseType.valueOf(type))) {
                    ++count;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getResponsesCount", ex);
        }

        return count;
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
            String cql = "select * from requests";
            ResultSet rs;
            if (taskuuid == null) {
                rs = session.execute("select * from requests order by agentuuid, reqseqnum;");
            } else {
                cql += " WHERE taskuuid = ? order by agentuuid, reqseqnum;;";
                PreparedStatement stmt = session.prepare(cql);

                BoundStatement boundStatement = new BoundStatement(stmt);
                rs = session.execute(boundStatement.bind(taskuuid));
            }

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
                if (row.getString("outputredirectionstderr") != null) {
                    request.setStdErr(OutputRedirection.valueOf(row.getString("outputredirectionstderr")));
                }
                request.setStdErrPath(row.getString("erroutpath"));
                if (row.getString("outputredirectionstdout") != null) {
                    request.setStdOut(OutputRedirection.valueOf(row.getString("outputredirectionstdout")));
                }
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

    @Override
    public Task getTask(UUID uuid) {
        Task task = new Task();
        try {
            String cql = "select * from tasks where uuid = ?";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(uuid));
            for (Row row : rs) {
                task.setUuid(row.getUUID("uuid"));
                task.setDescription(row.getString("description"));
                task.setTaskStatus(TaskStatus.valueOf(row.getString("status")));
            }


        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getTask", ex);
        }
        return task;
    }

    @Override
    public boolean truncateTables() {
        try {
            session.execute("truncate agents");
            session.execute("truncate tasks");
            session.execute("truncate requests");
            session.execute("truncate responses");
            session.execute("truncate cassandra_cluster_info");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getTasks", ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean saveCassandraClusterInfo(CassandraClusterInfo cluster) {
        try {
            String cql = "insert into cassandra_cluster_info (uid, name, commitlogdir, datadir, "
                    + "nodes, savedcachedir, seeds) "
                    + "values (?,?,?,?,?,?,?)";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(cluster.getUuid(), cluster.getName(),
                    cluster.getCommitLogDir(), cluster.getDataDir(), cluster.getNodes(),
                    cluster.getSavedCacheDir(), cluster.getSeeds()));

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveCassandraClusterInfo", ex);
            return false;
        }
        return true;
    }

    @Override
    public List<CassandraClusterInfo> getCassandraClusterInfo() {
        List<CassandraClusterInfo> list = new ArrayList<CassandraClusterInfo>();
        try {
            String cql = "select * from cassandra_cluster_info";
//            PreparedStatement stmt = session.prepare(cql);
//            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(cql);
            for (Row row : rs) {
                CassandraClusterInfo cd = new CassandraClusterInfo();
                cd.setUuid(row.getUUID("uid"));
                cd.setName(row.getString("name"));
                cd.setDataDir(row.getString("datadir"));
                cd.setSavedCacheDir(row.getString("savedcachedir"));
                cd.setCommitLogDir(row.getString("commitlogdir"));
                cd.setNodes(row.getList("nodes", UUID.class));
                cd.setSeeds(row.getList("seeds", UUID.class));
                list.add(cd);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getCassandraClusterInfo", ex);
        }
        return list;
    }

    @Override
    public boolean saveHadoopClusterInfo(HadoopClusterInfo cluster) {
        try {
            String cql = "insert into hadoop_cluster_info (uid, cluster_name, name_node, secondary_name_node, "
                    + "job_tracker, replication_factor, data_nodes, task_trackers, ip_mask) "
                    + "values (?,?,?,?,?,?,?,?,?)";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            session.execute(boundStatement.bind(cluster.getUid(),
                    cluster.getClusterName(), cluster.getNameNode(),
                    cluster.getSecondaryNameNode(), cluster.getJobTracker(),
                    cluster.getReplicationFactor(), cluster.getDataNodes(),
                    cluster.getTaskTrackers(), cluster.getIpMask()));
            return true;

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveHadoopClusterInfo", ex);
        }
        return false;
    }

    @Override
    public List<HadoopClusterInfo> getHadoopClusterInfo() {
        List<HadoopClusterInfo> list = new ArrayList<HadoopClusterInfo>();
        try {
            String cql = "select * from hadoop_cluster_info";
//            PreparedStatement stmt = session.prepare(cql);
//            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(cql);
            for (Row row : rs) {
                HadoopClusterInfo cd = new HadoopClusterInfo();
                cd.setUid(row.getUUID("uid"));
                cd.setClusterName(row.getString("cluster_name"));
                cd.setNameNode(row.getUUID("name_node"));
                cd.setSecondaryNameNode(row.getUUID("secondary_name_node"));
                cd.setJobTracker(row.getUUID("job_tracker"));
                cd.setReplicationFactor(row.getInt("replication_factor"));
                cd.setDataNodes(row.getList("data_nodes", UUID.class));
                cd.setTaskTrackers(row.getList("task_trackers", UUID.class));
                cd.setIpMask(row.getString("ip_mask"));
                list.add(cd);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHadoopClusterInfo", ex);
        }
        return list;
    }

    public CassandraClusterInfo getCassandraClusterInfo(String clusterName) {
        CassandraClusterInfo cassandraClusterInfo = null;
        try {
            String cql = "select * from cassandra_cluster_info where name = ? limit 1 allow filtering";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(clusterName));
            Row row = rs.one();
            if (row != null) {
                cassandraClusterInfo = new CassandraClusterInfo();
                cassandraClusterInfo.setUuid(row.getUUID("uid"));
                cassandraClusterInfo.setName(row.getString("name"));
                cassandraClusterInfo.setCommitLogDir(row.getString("commitlogdir"));
                cassandraClusterInfo.setDataDir(row.getString("datadir"));
                cassandraClusterInfo.setSavedCacheDir(row.getString("savedcachedir"));
                cassandraClusterInfo.setNodes(row.getList("nodes", UUID.class));
                cassandraClusterInfo.setSeeds(row.getList("seeds", UUID.class));
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getCassandraClusterInfo(name)", ex);
        }
        return cassandraClusterInfo;
    }

    public HadoopClusterInfo getHadoopClusterInfo(String clusterName) {
        HadoopClusterInfo hadoopClusterInfo = null;
        try {
            String cql = "select * from hadoop_cluster_info where cluster_name = ? limit 1 allow filtering";
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            ResultSet rs = session.execute(boundStatement.bind(clusterName));
            Row row = rs.one();
            if (row != null) {
                hadoopClusterInfo = new HadoopClusterInfo();
                hadoopClusterInfo.setUid(row.getUUID("uid"));
                hadoopClusterInfo.setClusterName(row.getString("cluster_name"));
                hadoopClusterInfo.setNameNode(row.getUUID("name_node"));
                hadoopClusterInfo.setSecondaryNameNode(row.getUUID("secondary_name_node"));
                hadoopClusterInfo.setJobTracker(row.getUUID("job_tracker"));
                hadoopClusterInfo.setReplicationFactor(row.getInt("replication_factor"));
                hadoopClusterInfo.setDataNodes(row.getList("data_nodes", UUID.class));
                hadoopClusterInfo.setTaskTrackers(row.getList("task_trackers", UUID.class));
                hadoopClusterInfo.setIpMask(row.getString("ip_mask"));
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHadoopClusterInfo(name)", ex);
        }
        return hadoopClusterInfo;
    }
}
