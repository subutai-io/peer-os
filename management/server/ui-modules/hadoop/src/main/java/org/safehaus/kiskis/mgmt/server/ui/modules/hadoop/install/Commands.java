package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.install;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/7/13
 * Time: 5:10 PM
 */
public class Commands {
    public static final String INSTALL_DEB = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/apt-get update && /usr/bin/apt-get\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"--force-yes\",\"--assume-yes\",\"install\",\"ksks-hadoop\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

     public static final String SET_MASTERS = "{\n" +
             "\t  \"command\": {\n" +
             "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
             "\t    \"source\": :source,\n" +
             "\t    \"uuid\": :uuid,\n" +
             "\t    \"taskUuid\": :taskUuid,\n" +
             "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
             "\t    \"workingDirectory\": \"/\",\n" +
             "\t    \"program\": \". /etc/profile && hadoop-configure.sh\",\n" +
             "\t    \"stdOut\": \"RETURN\",\n" +
             "\t    \"stdErr\": \"RETURN\",\n" +
             "\t    \"runAs\": \"root\",\n" +
             "\t    \"args\": [\n" +
             "\t      \":namenode:8020\",\":jobtracker:9000\",\":replicationfactor\"\n" +
             "\t    ],\n" +
             "\t    \"timeout\": 180\n" +
             "\t  }\n" +
             "\t}";

    public static final String CLEAR_MASTER = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"masters\",\"clear\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String SET_SECONDARY_NAME_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"masters\",\":secondarynamenode\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String CLEAR_SLAVES = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"slaves\",\"clear\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String SET_SLAVES = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"slaves\",\":slave-hostname\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String SET_MASTER_KEY = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/bin/mkdir -p /root/.ssh && chmod 700 /root/.ssh && ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"timeout\": 360\n" +
            "\t  }\n" +
            "\t}";

    public static final String COPY_MASTER_KEY = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/bin/cat\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"/root/.ssh/id_dsa.pub\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String PASTE_MASTER_KEY = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/bin/mkdir -p /root/.ssh && chmod 700 /root/.ssh && /bin/echo\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"':PUB_KEY'\",\">>\",\"/root/.ssh/authorized_keys && chmod 644 /root/.ssh/authorized_keys\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String SET_MASTER_CONFIG = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"echo 'Host *' > /root/.ssh/config " +
            "&& echo '    StrictHostKeyChecking no' >> /root/.ssh/config " +
            "&& chmod 644 /root/.ssh/config\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String FORMAT_NAME_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"namenode\",\"-format\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String REMOVE_DATA_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"slaves\",\"clear\",\":slave-hostname\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String EXCLUDE_DATA_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"dfs.exclude\",\":IP\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String REFRESH_DATA_NODES = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"dfsadmin\",\"-refreshNodes\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String STOP_DATA_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"hadoop-daemon.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"stop\",\"datanode\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String STATUS_DATA_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/service\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"hadoop-all\",\"status\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String ADD_DATA_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"slaves\",\":slave-hostname\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String START_DATA_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-daemon.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"start\",\"datanode\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String INCLUDE_DATA_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"dfs.include\",\":IP\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String READ_HOSTNAME = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"cat\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"/etc/hosts\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String WRITE_HOSTNAME = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"echo \",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"':hosts'\", \">\",\"/etc/hosts\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String COMMAND_NAME_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/service\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"hadoop-dfs\",\":command\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String COMMAND_JOB_TRACKER = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/service\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"hadoop-mapred\",\":command\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";
}
