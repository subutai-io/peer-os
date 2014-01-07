package org.safehaus.kiskis.mgmt.shared.protocol.api;

import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 10:40 PM
 */
public interface CommandManagerInterface {

    boolean executeCommand(Command command);

    void addListener(CommandListener listener);

    void removeListener(CommandListener listener);

    public List<Request> getCommands(UUID taskUuid);

    public Integer getResponseCount(UUID taskuuid);

    public Integer getSuccessfullResponseCount(UUID taskuuid);

    public Response getResponse(UUID taskuuid, Integer requestSequenceNumber);

    public List<ParseResult> parseTask(UUID taskUuid, boolean isResponseDone);

    public void saveResponse(Response response);

    public String saveTask(Task task);

    public List<Task> getTasks();

    public Task getTask(UUID uuid);

    public boolean truncateTables();

    public boolean saveCassandraClusterData(CassandraClusterInfo cluster);

    public boolean deleteCassandraClusterData(UUID uuid);

    public List<CassandraClusterInfo> getCassandraClusterData();

    public List<HadoopClusterInfo> getHadoopClusterData();

    public HadoopClusterInfo getHadoopClusterData(String clusterName);

    public boolean saveHadoopClusterData(HadoopClusterInfo cluster);
}
