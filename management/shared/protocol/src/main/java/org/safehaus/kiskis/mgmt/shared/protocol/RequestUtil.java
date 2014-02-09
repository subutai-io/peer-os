package org.safehaus.kiskis.mgmt.shared.protocol;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import java.util.ArrayList;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.DbManager;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/23/13 Time: 3:11 PM
 */
public class RequestUtil {

    private static final Logger LOG = Logger.getLogger(RequestUtil.class.getName());
    private static final DbManager dbManager;

    static {
        dbManager = ServiceLocator.getService(DbManager.class);
    }

    public static Task createTask(String description) {
        Task task = new Task();
        task.setTaskStatus(TaskStatus.NEW);
        task.setDescription(description);
        saveTask(task);
        return task;
    }

    public static boolean saveCommand(Command command) {
        try {
            String cql = "insert into requests (source, reqseqnum, type, agentuuid, taskuuid, "
                    + "workingdirectory, program, outputredirectionstdout, outputredirectionstderr, "
                    + "stdoutpath, erroutpath, runsas, args, environment, pid, timeout) "
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

            Request request = command.getRequest();
            dbManager.executeUpdate(cql, request.getSource(), request.getRequestSequenceNumber(),
                    request.getType() + "", request.getUuid(), request.getTaskUuid(), request.getWorkingDirectory(),
                    request.getProgram(), request.getStdOut() + "", request.getStdErr() + "",
                    request.getStdOutPath(), request.getStdErrPath(), request.getRunAs(), request.getArgs(),
                    request.getEnvironment(), request.getPid(), request.getTimeout());
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveCommand", ex);
        }
        return false;
    }

    public static boolean saveResponse(Response response) {
        try {

            String cql = "insert into responses (agentuuid, taskuuid, resseqnum, exitcode, errout, hostname, ips, "
                    + "macaddress, pid, reqseqnum, responsetype, source, stdout, islxc) "
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            dbManager.executeUpdate(cql, response.getUuid(), response.getTaskUuid(),
                    response.getResponseSequenceNumber() == null ? -1 : response.getResponseSequenceNumber(),
                    response.getExitCode() == null ? -1 : response.getExitCode(), response.getStdErr(),
                    response.getHostname(), response.getIps(),
                    response.getMacAddress(), response.getPid(),
                    response.getRequestSequenceNumber() == null ? -1 : response.getRequestSequenceNumber(),
                    response.getType().toString(),
                    response.getSource(), response.getStdOut(), response.isLxc);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveResponse", ex);
        }
        return false;
    }

    public static String saveTask(Task task) {
        try {
            String cql = "insert into tasks (uuid, description, status) "
                    + "values (?, ?, ?);";

            dbManager.executeUpdate(cql, task.getUuid(), task.getDescription(), task.getTaskStatus().toString());

            return task.getUuid().toString();

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveTask", ex);
        }
        return null;
    }

    public static Task getTask(UUID uuid) {
        Task task = new Task();
        try {
            String cql = "select * from tasks where uuid = ?";
            ResultSet rs = dbManager.executeQuery(cql, uuid);
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

    public static Integer getResponsesCount(UUID taskUuid) {
        Integer count = 0;
        try {
            String cql = "select * from responses where taskuuid = ?;";
            ResultSet rs = dbManager.executeQuery(cql, taskUuid);
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

    public static List<Response> getResponses(UUID taskuuid, Integer requestSequenceNumber) {
        List<Response> list = new ArrayList<Response>();
        try {
            String cql = "select * from responses "
                    + "WHERE taskuuid = ? "
                    + "and reqseqnum = ? and resseqnum >= 0 "
                    + "ORDER BY reqseqnum, resseqnum";
            ResultSet rs = dbManager.executeQuery(cql, taskuuid, requestSequenceNumber);
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

    public static List<Request> getRequests(UUID taskuuid) {
        List<Request> list = new ArrayList<Request>();
        try {
            if (taskuuid != null) {
                String cql = "select * from requests WHERE taskuuid = ? order by agentuuid, reqseqnum;";
                ResultSet rs = dbManager.executeQuery(cql, taskuuid);

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
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRequests", ex);
        }
        return list;
    }

    public static Response getResponse(UUID taskuuid, Integer requestSequenceNumber) {
        Response response = null;
        try {
            List<Response> list = getResponses(taskuuid, requestSequenceNumber);

            String stdOut = "", stdErr = "";
            for (Response r : list) {
                response = r;
                if (r.getStdOut() != null && !r.getStdOut().equalsIgnoreCase("null") && !Util.isStringEmpty(r.getStdOut())) {
                    stdOut += r.getStdOut();
                }
                if (r.getStdErr() != null && !r.getStdErr().equalsIgnoreCase("null") && !Util.isStringEmpty(r.getStdErr())) {
                    stdErr += r.getStdErr();
                }
            }

            if (response != null) {
                response.setStdOut(stdOut);
                response.setStdErr(stdErr);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getResponse", ex);
        }
        return response;
    }

    public static List<ParseResult> parseTask(UUID taskUuid, boolean isResponseDone) {
        List<ParseResult> result = new ArrayList<ParseResult>();
        try {

            List<Request> requestList = getRequests(taskUuid);
            Integer responseCount = getResponsesCount(taskUuid);

            if (isResponseDone) {
                if (requestList.size() != responseCount) {
                    return result;
                }
            }

            Integer exitCode = 0;
            for (Request request : requestList) {
                Response response = getResponse(taskUuid, request.getRequestSequenceNumber());
                if (response != null) {
                    result.add(new ParseResult(request, response));
                    if (response.getType().compareTo(ResponseType.EXECUTE_RESPONSE_DONE) == 0) {
                        exitCode += response.getExitCode();
                    } else if (response.getType().compareTo(ResponseType.EXECUTE_TIMEOUTED) == 0) {
                        exitCode = 1;
                    }
                }
            }

            if (requestList.size() == responseCount) {
                Task task = getTask(taskUuid);
                if (exitCode == 0) {
                    task.setTaskStatus(TaskStatus.SUCCESS);
                } else {
                    task.setTaskStatus(TaskStatus.FAIL);
                }
                saveTask(task);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in parseTask", e);
        }

        return result;
    }

    public static Request createRequest(CommandManager manager, final String command, Task task, HashMap<String, String> map) {
        String json = command;

        json = json.replaceAll(":taskUuid", task.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());

        for (String key : map.keySet()) {
            json = json.replaceAll(key, map.get(key));
        }

        Request request = CommandJson.getRequest(json);
        if (manager != null) {
            manager.executeCommand(new CommandImpl(request));
        }

        return request;
    }

}
