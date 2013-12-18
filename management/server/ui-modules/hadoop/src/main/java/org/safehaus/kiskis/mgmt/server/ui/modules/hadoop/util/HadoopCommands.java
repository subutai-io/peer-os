package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.util;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/7/13
 * Time: 5:10 PM
 */
public class HadoopCommands {
    public static final String INSTALL_HADOOP = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/apt-get\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"--force-yes\",\"--assume-yes\",\"install\",\"ksks-hadoop\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

     public static final String CONFIGURE_SLAVES = "{\n" +
             "\t  \"command\": {\n" +
             "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
             "\t    \"source\": :source,\n" +
             "\t    \"uuid\": :uuid,\n" +
             "\t    \"taskUuid\": :taskUuid,\n" +
             "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
             "\t    \"workingDirectory\": \"/\",\n" +
             "\t    \"program\": \"hadoop-configure.sh\",\n" +
             "\t    \"stdOut\": \"RETURN\",\n" +
             "\t    \"stdErr\": \"RETURN\",\n" +
             "\t    \"runAs\": \"root\",\n" +
             "\t    \"args\": [\n" +
             "\t      \"hdfs://:namenode:8020\",\":jobtracker:9000\",\":replicationfactor\"\n" +
             "\t    ],\n" +
             "\t    \"timeout\": 180\n" +
             "\t  }\n" +
             "\t}";

    public static final String CLEAR_SECONDARY_NAME_NODE = "{\n" +
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

    public static final String CLEAR_SLAVES_NAME_NODE = "{\n" +
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

    public static final String SET_SLAVES_NAME_NODE = "{\n" +
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

    public static final String CLEAR_SLAVES_JOB_TRACKER = "{\n" +
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

    public static final String SET_SLAVES_JOB_TRACKER = "{\n" +
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

    public static final String SET_SSH_MASTERS = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/bin/mkdir /home/.ssh && ssh-keygen -t dsa -P '' -f /home/.ssh/id_dsa\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String COPY_SSH_SLAVES = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/bin/echo\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \":PUB_KEY\",\">>\",\"home/.ssh/authorized_keys\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String CONFIG_SSH_MASTER = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"echo \"Host *\" >> /home/.ssh/config && echo \"StrictHostKeyChecking no\" >> /home/.ssh/config\",\n" +
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
}
