package org.safehaus.subutai.plugin.solr.impl;


import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.common.protocol.RequestBuilder;

import com.google.common.collect.Sets;


public class Commands
{

    public static String startCommand = "service solr start";
    public static String stopCommand = "service solr stop";
    public static String statusCommand = "service solr status";
}
